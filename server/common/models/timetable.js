'use strict';

process.env.TZ = 'etc/UTC';

const rp = require('request-promise');
const cheerio = require('cheerio');
const Moment = require('moment');
const MomentRange = require('moment-range');
const moment = MomentRange.extendMoment(Moment);

let cookiejar = rp.jar();
let states = {};
let i = 0;

function get(pattern, string) {
  let m;
  let ms = [];
  do {
    m = pattern.exec(string);
    if (m) {
      ms.push(m);
    }
  } while (m);
  return ms;
}

function transform(body) {
  let $ = cheerio.load(body);
  states = {
    viewstate: $('#__VIEWSTATE').val() || '',
    viewstategen: $('#__VIEWSTATEGENERATOR').val() || '',
    eventval: $('#__EVENTVALIDATION').val() || '',
  };
  return $;
}

function hent(data, cb) {
  let date = moment.utc(data.date);
  console.log('Yes, master, I will get your stuff.');
  let options = {
    method: 'GET',
    url: 'https://selvbetjening.aarhustech.dk/WebTimeTable/default.aspx',
    qs: {
      viewdate: date.format('DD-MM-YYYY')
    },
    jar: cookiejar,
    headers: {
      Referer: 'https://selvbetjening.aarhustech.dk/WebTimeTable/default.aspx',
      Connection: 'keep-alive',
      'Cache-Control': 'no-cache',
      Accept: '*/*',
      'Content-Type': 'application/x-www-form-urlencoded',
      'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36',
      'Accept-Language': 'en-US,en;q=0.9',
      Origin: 'https://selvbetjening.aarhustech.dk',
      Pragma: 'no-cache',
    }
  };
  // console.log(options);
  rp(options).then(function (body) {
    let $ = transform(body);
    if ($('#forms_login').length > 0) {
      login(function callback() {
        hent(data, cb);
      });
    } else if ($('#SchemaPlaceHolder').length > 0) {
      actualHent(data, cb);
    } else {
      console.log('The hell?!')
    }
  }).catch((err) => {
    console.log(1, err);
  });
}

function actualHent(data, cb) {
  let date = moment.utc(data.date);

  let options = {
    method: 'POST',
    url: 'https://selvbetjening.aarhustech.dk/WebTimeTable/default.aspx',
    jar: cookiejar,
    headers: {
      Referer: 'https://selvbetjening.aarhustech.dk/WebTimeTable/default.aspx',
      'X-MicrosoftAjax': 'Delta=true',
      Connection: 'keep-alive',
      'X-Requested-With': 'XMLHttpRequest',
      'Cache-Control': 'no-cache',
      Accept: '*/*',
      'Content-Type': 'application/x-www-form-urlencoded',
      'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36',
      'Accept-Language': 'en-US,en;q=0.9',
      Origin: 'https://selvbetjening.aarhustech.dk',
      Pragma: 'no-cache',
    },
    form: {
      Login: data.login,
      Domain: data.domain,
      Type: data.type,
      currentViewDate: date.format('DD-MM-YYYY'),
      ScriptManager1: 'RadAjaxPanel1Panel|DayViewButton',
      __VIEWSTATE: states.viewstate,
      __VIEWSTATEGENERATOR: states.viewstategen,
      __EVENTVALIDATION: states.eventval,
      currentViewType: 'SchemaView',
      __ASYNCPOST: 'true',
      'DayViewButton.x': '1',
      'DayViewButton.y': '1',
    },
  };
  // console.log(options);
  rp(options).then(function (body) {
    extract(body, data, cb);
  }).catch((err) => {
    console.log(1, err);
  });
}

function escapeRegExp(str) {
  return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, '\\$&');
}

function extract(body, data, cb) {
  // console.log(body);
  let $ = transform(body);
  let lessons = [];
  let date = moment.utc(data.date);
  $('#innerCalendar #day0Col div[onmouseover]').each(function (i, elem) {
    let t = $(elem).attr('onmouseover');
    // console.log(t);
    let time = get(/(?:Time|Tid): (\d\d):(\d\d)-(\d\d):(\d\d)/g, t)[0];
    let groups = get(/(?:Subject|Emne): (.*?)<br>/g, t)[0];
    let rooms = get(/(?:Room|Lokale): (((D\d\d\d\d(-\d)?)(, )?)*)/g, t)[0];
    let teachers = get(/(?:Teacher|Lærer): (.*?) - (.*?)<br>/g, t);
    let course = get(/(?:Course|Fag): (.*?)<br>/g, t)[0];
    // console.log(groups);

    if (course === undefined) {
      course = ['', groups[1].split(/, +/)[0]];
    }

    lessons.push({
      startTime: date.set({
        'hour': parseInt(time[1]),
        'minute': parseInt(time[2]),
        'second': 0,
      }).toDate(),
      endTime: date.set({
        'hour': parseInt(time[3]),
        'minute': parseInt(time[4]),
        'second': 0,
      }).toDate(),
      groups: groups[1].replace(new RegExp(escapeRegExp(course[1]) + ' ?'), '').split(/, +/).filter(String),
      rooms: rooms[1].split(/, +/).filter(String),
      teachers: teachers.map(x => x[1] + ' (' + x[2] + ')').filter(String),
      subject: course[1],
    });
  });
  // console.log(lessons);
  cb(lessons);
}

function login(callback) {
  i++;
  if (i > 1) {
    process.exit();
  }
  console.log(process.env);
  let options = {
    method: 'POST',
    url: 'https://selvbetjening.aarhustech.dk/UMSLogin/weblogin.aspx',
    qs: {
      ReturnUrl: '%2fWebTimeTable%2fdefault.aspx',
    },
    jar: cookiejar,
    simple: false,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-MicrosoftAjax': 'Delta=true',
      Connection: 'keep-alive',
      'X-Requested-With': 'XMLHttpRequest',
      'Cache-Control': 'no-cache',
      Accept: '*/*',
      'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36',
      'Accept-Language': 'en-US,en;q=0.9',
      Origin: 'https://selvbetjening.aarhustech.dk',
      Pragma: 'no-cache',
    },
    form: {
      txtUsername: new Buffer(process.env.UVMUSERNAME, 'base64').toString('ascii'),
      txtPassword: new Buffer(process.env.UVMPASSWORD, 'base64').toString('ascii'),
      RADScriptMan: 'RadAjaxPanel1Panel|btnLogin',
      __ASYNCPOST: 'true',
      __EVENTTARGET: 'btnLogin',
      __VIEWSTATE: states.viewstate,
      __VIEWSTATEGENERATOR: states.viewstategen,
      __EVENTVALIDATION: states.eventval,
      RadAJAXControlID: 'RadAjaxPanel1',
    },
  };

  rp(options).then(function (body) {
    transform(body);
    console.log('Got ze state.');
    callback();
  }).catch((err) => {
    console.log(2, err);
  });
}

module.exports = function (Timetable) {
  Timetable.get = function (date, teacher, room, student, group, force, cb) {
    i = 0;
    let data = {
      date: date,
    };
    let timetable = {
      date: date,
    };
    if (teacher) {
      data['login'] = teacher;
      data['domain'] = 'adm.ats.dk';
      data['type'] = 'teacher';
      timetable.teacher = teacher;
    } else if (room) {
      data['login'] = room;
      data['domain'] = '1';
      data['type'] = 'room';
      timetable.room = room;
    } else if (student) {
      data['login'] = student;
      data['domain'] = 'edu.ats.dk';
      data['type'] = 'S';
      timetable.student = student;
    } else if (group) {
      data['login'] = group;
      data['domain'] = '';
      data['type'] = 'course';
      timetable.group = group;
    }

    console.log('Getting: ' + JSON.stringify(data));

    let m = moment.utc(date);

    function freeRooms(tt) {
      if (room) {
        let possibles = [
          [8, 15, 9, 15],
          [9, 35, 10, 35],
          [10, 45, 11, 45],
          [12, 15, 13, 15],
          [13, 25, 14, 25],
          [14, 30, 15, 30],
        ].map(function (x) {
          return moment.range(m.clone().set({
            'hour': x[0],
            'minute': x[1],
            'second': 0,
          }), m.clone().set({
            'hour': x[2],
            'minute': x[3],
            'second': 0,
          }));
        });
        possibles.forEach(function (possible) {
          let diditoverlap = false;
          tt.lessons.forEach(function (lesson) {
            let lessonrange = moment.range(lesson.startTime, lesson.endTime);
            if (possible.overlaps(lessonrange)) {
              diditoverlap = true;
            }
          });
          if (!diditoverlap) {
            tt.lessons.push({
              startTime: possible.start.toDate(),
              endTime: possible.end.toDate(),
              isFree: true,
            });
          }
        });
      }
      tt.lessons.sort(function (a, b) {
        return a.startTime - b.startTime;
      });
      cb(null, tt);
    }

    function hentCreateSend(old) {
      hent(data, function (lessons) {
        timetable.lessons = lessons;
        Timetable.create(timetable, function (err, tt) {
          if (err) {
            return err;
          }
          timetable.lessons.forEach(function (lesson) {
            tt.lessons.create(lesson);
          });
          if (old) {
            old.forEach(function (lesson) { 
              let l = {
                subject: lesson.subject,
                startTime: lesson.startTime,
                endTime: lesson.endTime,
                booker: lesson.booker,
                rooms: lesson.rooms,
                teachers: lesson.teachers,
                groups: lesson.groups,
              };
              tt.lessons.create(l);
              timetable.lessons.push(l);
            });
          }
          freeRooms(timetable);
        });
      });
    }

    Timetable.findOne({
      where: timetable,
      include: 'lessons',
    }, function (err, found) {
      if (found === null) {
        console.log('Missed cache');
        hentCreateSend();
      } else {
        console.log('Hit cache!');
        if (force) {
          console.log('Destroy zhem aaaaall! (clear cache)');
          found.lessons.destroyAll({
            where: {
              'booker': null,
            },
          });
          let oldLessons = Array.from(found.__data.lessons.filter(function (x) {
            return x.booker === null
          }));
          found.destroy();
          hentCreateSend(oldLessons);
        } else {
          let tt = found.__data; // Ugly fix
          tt.lessons = Array.from(tt.lessons); // Cont. ugly fix
          freeRooms(tt);
        }
      }
    });
  };
  Timetable.remoteMethod(
    'get', {
      http: {
        path: '/get',
        verb: 'get',
      },
      returns: {
        arg: 'timetable',
        type: 'object',
        model: 'Timetable',
        root: true,
      },
      accepts: [{
        arg: 'date',
        type: 'date',
        required: true,
      }, {
        arg: 'teacher',
        type: 'string',
      }, {
        arg: 'room',
        type: 'string',
      }, {
        arg: 'student',
        type: 'string',
      }, {
        arg: 'group',
        type: 'string',
      }, {
        arg: 'force',
        type: 'boolean',
      }],
    }
  );
  Timetable.bookLesson = function (date, startTime, room, booker, cb) {
    console.log('Booking', date, startTime, room, booker);
    let m = moment.utc(startTime);
    Timetable.findOne({
      where: {
        date: date,
        room: room,
      },
      include: 'lessons',
    }, function (err, found) {
      if (found === null) {
        console.log('crap');
        cb(null, false);
      } else {
        found.lessons.create({
          startTime: startTime,
          endTime: m.add(1, 'h').toDate(),
          booker: booker,
          rooms: [room],
          subject: 'Booket lokale',
          teachers: [],
          groups: [],
        });
        cb(null, true);
      }
    });
  };
  Timetable.remoteMethod(
    'bookLesson', {
      http: {
        path: '/bookLesson',
        verb: 'post',
      },
      returns: {
        'arg': 'success',
        'type': 'boolean',
      },
      accepts: [{
        arg: 'date',
        type: 'date',
        required: true,
      }, {
        arg: 'startTime',
        type: 'date',
        required: true,
      }, {
        arg: 'room',
        type: 'string',
        required: true,
      }, {
        arg: 'booker',
        type: 'string',
        required: true,
      }],
    }
  );
};
