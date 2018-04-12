const rp = require('request-promise');
const cheerio = require('cheerio');
const dateformat = require('dateformat');
var cookiejar = rp.jar();
var viewstate = '';
var i = 0;

function get(pattern, string) {
	var m;
	var ms = [];
	do {
		m = pattern.exec(string);
		if (m) {
			ms.push(m);
		}
	} while (m);
	return ms;
}

function hent(data) {
	console.log('Yes, master, I will get your stuff.');
	var options = {
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
			Pragma: 'no-cache'
		},
		form: {
			Login: data.login,
			Domain: data.domain,
			Type: data.type,
			currentViewDate: '28-05-2018',
			ScriptManager1: 'RadAjaxPanel1Panel|PeriodNextButton',
			__VIEWSTATE: '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',
			currentViewType: 'DayView',
			__ASYNCPOST: 'true',
			'PeriodNextButton.x': '1',
			'PeriodNextButton.y': '1'
		}
	};
	//console.log(options);
	rp(options).then(function(body) {
		//console.log(body);
		if (body === '1|#||4|76|pageRedirect||%2fUMSLogin%2fweblogin.aspx%3fReturnUrl%3d%252fWebTimeTable%252fdefault.aspx|') {
			console.log('I\'m gaining sentience!');
			login(function callback() {
				hent(data);
			});
		} else if (body.startsWith('1|#||4|30|pageRedirect||%2fWebTimeTable%2fdefault.aspx|')) {
			hent(data);
		} else {
			extract(body);
		}
	}).catch((err) => {
		console.log(1, err);
	});
}

function extract(body) {
	var $ = cheerio.load(body);
	var lessons = [];
	$('table#DayViewTable tr:nth-child(2) > td:not(:first-child) div[onmouseover]').each(function(i, elem) {
		var t = $(elem).attr('onmouseover');
		console.log(t);
		var time = get(/Time: (\d\d):(\d\d)-(\d\d):(\d\d)/g, t)[0];
		var groups = get(/Subject: (.*?)<br>/g, t)[0];
		var rooms = get(/Room: (((D\d\d\d\d(-\d)?)(, )?)*)/g, t)[0];
		var teachers = get(/Teacher: (.*?) - (.*?)<br>/g, t);
		var course = get(/Course: (.*?)<br>/g, t)[0];

		lessons.push({
			startTime: time[1] + ':' + time[2],
			endTime: time[3] + ':' + time[4],
			groups: groups[1].replace(course[1] + ' ', '').split(/[, ]+/),
			rooms: rooms[1].split(/[, ]+/),
			teachers: teachers.map(x => x[1] + ' (' + x[2] + ')'),
			course: course[1]
		});
	});
	console.log(lessons);
}

function login(callback) {
	i += 1;
	console.log(i);
	if (i > 2) {
		process.exit();
	}
	var options = {
		method: 'POST',
		url: 'https://selvbetjening.aarhustech.dk/UMSLogin/weblogin.aspx',
		qs: {
			ReturnUrl: '%2fWebTimeTable%2fdefault.aspx'
		},
		transform: function(body) {
			return cheerio.load(body);
		},
		jar: cookiejar,
		simple: false,
		headers: {
			'Postman-Token': 'bf374d86-2a39-14df-8167-dbcdae091d01',
			'Content-Type': 'application/x-www-form-urlencoded',
			'X-MicrosoftAjax': 'Delta=true',
			Connection: 'keep-alive',
			'X-Requested-With': 'XMLHttpRequest',
			'Cache-Control': 'no-cache',
			Accept: '*/*',
			'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36',
			'Accept-Language': 'en-US,en;q=0.9',
			Origin: 'https://selvbetjening.aarhustech.dk',
			Pragma: 'no-cache'
		},
		form: {
			txtUsername: new Buffer("amFjbzk1MzQ=", 'base64').toString('ascii'),
			txtPassword: new Buffer("bXVtOTR3cm4=", 'base64').toString('ascii'),
			RADScriptMan: 'RadAjaxPanel1Panel|btnLogin',
			__ASYNCPOST: 'true',
			__EVENTTARGET: 'btnLogin',
			__VIEWSTATE: 'wJA2tKF+BWay2aw53Bk6k0l5Sh4YmKuTgAJDlErzgAkFTI2UtkYmCkqUQR7KVScz5p56KtKs5rm21ZIx7j8gWWrLPXGejTFl98LJtDIJOaLykTPA5FFPKmoMlll5beDeIOQD5Hj4auW48wZ9kMRLQwq+fHMGp0EqXyYUCTEr5WFaYprNhy5kxmE1VnnCn4C3Vw44xx708Nv43Q5A5NguOcL4MD7Gsdhxel/A3K4/r+eGJ67UdrjrJItEe5W0MycsG/EUVNCsuE8f0TF0HVrOSmD67nV3tGwq1bbUbplNo6YsxcyP3vk04LGT64ToOoomDbmZEAVQYKBzY6r61/Cb8M+YleSbmGx6TfHit5s/2SK8Fgu3Iupr730DLgGJf90n7ruYl2vuG3HIDK/Z+EjR5e5Nsy73wfcJzE7BDTLVf7WQ0YhT76qzuoLj2J3tLcm3SxMEQRFUC3eKu0IRj8EDkJMD0ddgvbfIdvELZliCiiT2ecNV01vmBqu7F8Di2CSx5wuwh/7tEWy/nK53J9GAHvWegrDxhYpCkSGkoQsKoBXfQGce03C51dxQffoXoG2lgkeP5aKUdMGC5W9zbtGsX3M/on+qwPhIwQgudqYBUPFUqYsR3RFuqKNa5M62g+Z6eAzE1K+43yWqBD1532GTbLFUFp6FJyuioqzRyJcklRG18sXuiiZa61PvFP5MLUTRBU3LvKe4UiowtaA5e5zFZi/hi2+SE01Yq4YYLUDqFZK8Ghn4q4c3q0a3B63kIUOfIcT5MMcm4YOCkGiI0Li/2brMFXWbgfcWUG+bHvrTmHZF6n2ZLuAit/7LdhAucuDkMxn7om1dOh40h2JjaHCChasLQzS+4WycYR17xOLBVtya58lOPUWS151OFAUEskmC3sk0CBh6/0E4AvOTltNKWf3x4IeT9JGa4PFpLhDx6Ep0CLZkHLENLvYlV7vXmLv5nZSwiik1aSUd0ErrUGYBOtlVxeASwMrOjPMjrFsdf8mfvVg1RjZ5HC+0lOLXlTszNGtmjkzBiygl41qS8i/CkpH9JSW2/9GBlEHvdOPH+kzUvJj2OeR2a56sDfllVWH+l4kdlGFLoVNGLfw+sPQQ+eqxiVHC3XBJUnqf7xr8Yh9BKQZ8i7q/YEp9y2wgf2i+NOdOGoNuBv27BEp/GsGRv3H2mDnDrQyjC6OGXIrdbHMVK6s4c2FLgTpgwwNRYYA/7SSQz7s45lzQxeFYWxwPSfsgirtAXoxbCQWhqp1AmrgDy4hTY6ELjL5Cx/bC201NqwjFH2Skdn+/BjUSQ+N95k5tL2AObnG5TVY469Apjd2dZI2XfUCjdf/Fv6n88WTNSt0e3LMrnL5D/jHHTR2INoFal0fEK7p2oZfbrFojTOZWAsvn2jWBVW07x5h5WFsJvW50O06aB1jnaeVozc22Wmw3flnPSkeGN/0Hwe0c0rjt4r5uJ1YZb44lM3zP6Ej8dFCgcoksQ3RF1Acoopd9faLO+oc9yGI1rWY4GSxHbvfRc0VdlQmlxaQjClEzwVEiChG26SBZmWnXtlOGx5QqwQmEF9yd/NjinPlyMy8dfvJXAcp6zE7S2oDmCJt63EBRrV8f3vD97Ge4edeyj+WZkw==',
			__VIEWSTATEGENERATOR: '786FD344',
			__EVENTVALIDATION: 'wJa68xz6rVfV4N51sfGPg7z77JTCCe4NKP1JWttB8ZO7U08vNUojTF2prQ80QeB7RkMLM/lLTuogpB+9zch5QuCg19hCmACXTlbq/Ayup2a30khfletGt5lC5D+BKPFzII/ThhRXNDLn8lnnMdzDEDIQv+x6T2qhcN7R/JnC2cBxKycI6EwLY5ZPbPvlz+yfL7jJifPEHC37/iK/AgvEzlO1o8y1pQVbC0drznGSowA=',
			RadAJAXControlID: 'RadAjaxPanel1'
		}
	};

	rp(options).then(function($) {
		viewstate = $('#__VIEWSTATE').val();
		console.log('Got ze state.');
		callback();
	}).catch((err) => {
		console.log(2, err);
	});
};

hent({
	login: 'oliv5179',
	type: 'S',
	domain: 'edu.ats.dk'
});