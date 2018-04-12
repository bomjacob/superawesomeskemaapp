var request = require('request');

function dologin(callback){
    var headers = {
        'Pragma': 'no-cache',
        'Origin': 'https://selvbetjening.aarhustech.dk',
        'Accept-Language': 'en-GB,en-US;q=0.9,en;q=0.8',
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36',
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
        'Accept': '*/*',
        'Cache-Control': 'no-cache',
        'X-Requested-With': 'XMLHttpRequest',
        'Connection': 'keep-alive',
        'X-MicrosoftAjax': 'Delta=true',
        'Referer': 'https://selvbetjening.aarhustech.dk/UMSLogin/weblogin.aspx?ReturnUrl=%2floggedin%2fdefault.aspx',
        'Cookie': '__utmz=253913303.1519814211.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utma=253913303.1659794554.1519814211.1519814211.1521189299.2; __utmc=253913303; __utmt=1; __utmb=253913303.2.10.1521189299; ASP.NET_SessionId=ve5jy43znyiewowmrhl5zs3b'
    };

    var dataString = 'RADScriptMan=RadAjaxPanel1Panel%7CbtnLogin&__LASTFOCUS=&RADScriptMan_TSM=%3B%3BAjaxControlToolkit%2C%20Version%3D4.1.7.725%2C%20Culture%3Dneutral%2C%20PublicKeyToken%3D28f01b0e84b6d53e%3Aen-US%3Ab082e3da-7259-4ffe-a049-56d9b292f1bc%3Aea597d4b%3Ab25378d2%3BTelerik.Web.UI%2C%20Version%3D2017.3.913.45%2C%20Culture%3Dneutral%2C%20PublicKeyToken%3D121fae78165ba3d4%3Aen-US%3A03e3fdef-45f6-40a0-88ab-9645d53a0f37%3A16e4e7cd%3Aed16cbdc%3Af7645509%3A88144a7a%3A24ee1bba%3Ac128760b%3A19620875%3A874f8ea2%3Ac172ae1e%3Af46195d3%3A9cdfc6e7%3A33715776%3Ae330518b%3A2003d0b8%3A1e771326%3Ac8618e41%3Ae4f8f289%3A1a73651d%3A16d8629e&__EVENTTARGET=btnLogin&__EVENTARGUMENT=&__VIEWSTATE=qX5nQfXkA1tiHwzwbP64Z7IQf3pIk2fSILZZm9Fojcy9iNpcRtc5G9aErLBKrMzMW%2FQlmaAD8jCNPEWaEYhBdY4m01ff%2FV6yVeynIDwAfMJ4M21PzJh17N2gqe3hmmYLJRgISmOVrGBOGEPrO5Jt9N9eTuE%2FZhN05NnO2gHJvrfidJBoW%2FpZX%2FjexyzymYxBCwJLdt46hxLqbQF6sdFOqpqGQnIrbMIFx4jwBAp2PU1RYXqEiRuFqSPCeN0Rbnl2JN1DOXa40DoLl3Yksm16%2BatqdpNosc3yv3eevgPjOB44IMSycLUE3GxoJQeNSThGVZD1LVaDIbUHdnHzlmWknr320HHXweE8P8sN5Ae%2FD%2Fd3gjvnozzvIxrWeJ%2BAzguscl0%2F0jPKwV4puyFRg9M96DiOh3p64nQRN4ZJjd1uyjWMPMmtK5ifuvoE3FvDhI9sI2DqgRWdQdZE4HF5qzpigfyDYj4ewSNqexETulsDQ5xH38qPxfyawO%2FwyIFOvWmokrFdTZDCVdk8Ms2z3yeOUwSK0vvwdMaq1alYh6ekL3fyOjEfBjKz9hhpV%2BdcC0wtb4ti3SaQR%2FLwx4FB2Dh%2B8%2FAE12zkpHTgbmLfqC5kt2OHMxT7N475bbiRTV4I5JZshMswX7paxLh8CZQoRu%2BbAqHDSk2rvv8wY0DDoOwHZdSh9W4mTaHpZJgBYO0aYcI58zI4wJpLl%2FPucPs1uNnRIkRnc7Fk%2B7hGsFuFTRXnmrFVWnDtDmU5iKUJ6tA4Oj5Xpau2mdfRGxA6jm9yovEKEmU97TZZ8cT1cB3ob844UF0qA37zCkqD1cSSu1e1b02BEBOvHvkohzUkKWnaFFXSmmn9ZnkkuZOOzM4d1M9D40FzQSw9xLhN7eIz6h%2FrdQkz8IjVTLLG%2FIloLL14AtKm%2FOupSKm4qBUKYnvO1IAbRX7M4nW6Gjj9yKOT9fuCBSwKtFXyCXJ%2BOuewH%2BP0aTTwOfS4mHJJq98ZHk4L5ANazl49QPrp6ZyYQDaip06omUImBvjzaxK1o4TqIjJ7oP3bB%2Fxtk2O2WWz4xvSXIHfA1CYIEVAu5hWT70q%2BNJ2vrJVmfTiWQlmG%2BW4DRgXUdLqmRSnfcaCfYC97Hac7ZA8zARrA95DhFXoYDMGJWEEnNAKSRyY3tekovnfgchjigFGRzUpid%2FIiCKnfo8ggy5oD5CiTF1YlOqcmy96rQvYuzz%2BA7%2BG%2B3eRV9ohAOIM2e2L22%2BBPxjbYq%2F%2FD124bfDL4bvbYow%2FWHkhhXGviWgbBM%2BhSZLOTDcMBcXQ8xu7q3JjIIbQhKQr3lujUpdL7j47nLZjeAlxOs5y8ABnnv5NwHSKF%2FtdCPiL4DrM8ddPoyY7zOegIYfN2JcNiBPWYwO5BPeWAkMNMMiAYSnN1rIzh1FequQAkOVzGc3VSJLvMy73MYaGkylJDTs9NJXkfpTLrvx%2Bsepx2z29A8O2EHrCj0QdhImG%2B1ErtCsfkb%2BfIE9sVIqFkTl%2B1s7tLoZl%2B5OjVK1RgMtnui5YBzrCkrEbn5ubxUlBhuSOIYJVfgLvuvryK%2F8%2FflJluxSEc9FKOD9VVIjuJbJHjx1qAqy8Hm5C5yHyFews5NVv6ren4oJSrANFfuQ%3D%3D&__VIEWSTATEGENERATOR=786FD344&__EVENTVALIDATION=BinfAv4rAw8ADsnjlnT%2B3LKDf9XbyZhQQTpnYgIQ5iS6vLBDXVL1RZhdrC3p6LJ0xsgskGSFvsFpmK4Nsj73%2FEpFEs3T1rUzmZ1guFyVz%2BLrhqijClEsj9Czna2SpA7Nqp7e023wBaRZ%2BwUT1jWwTLBe%2Brvx1JaNksR%2BphstcU5k5pSahc%2BZSIdKeZjhV6yDQIkUCiqr5gQRr2n8Jjh7SJmuaaOHTKO3%2FENALlzG%2FvU%3D&hfvLanguage=danish&txtUsername=jaco9534&txtPassword=mum94wrn&notify_ClientState=&notify%24hiddenState=&notify_XmlPanel_ClientState=&notify_TitleMenu_ClientState=&__ASYNCPOST=true&RadAJAXControlID=RadAjaxPanel1';

    var options = {
        url: 'https://selvbetjening.aarhustech.dk/UMSLogin/weblogin.aspx?ReturnUrl=%2floggedin%2fdefault.aspx',
        method: 'POST',
        headers: headers,
        body: dataString
    };

    function innercallback(error, response, body) {
        if (!error && response.statusCode == 200) {
            console.log('Login ist done');
            callback();
        }
    }

    request(options, innercallback);
}

function timetable(type, login, domain, callback) {

    var headers = {
        'Pragma': 'no-cache',
        'Origin': 'https://selvbetjening.aarhustech.dk',
        'Accept-Language': 'en-GB,en-US;q=0.9,en;q=0.8',
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36',
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
        'Accept': '*/*',
        'Cache-Control': 'no-cache',
        'X-Requested-With': 'XMLHttpRequest',
        'Connection': 'keep-alive',
        'X-MicrosoftAjax': 'Delta=true',
        'Referer': 'https://selvbetjening.aarhustech.dk/WebTimeTable/default.aspx',
        'Cookie': '__utmz=253913303.1519814211.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utma=253913303.1659794554.1519814211.1519814211.1521189299.2; __utmc=253913303; __utmt=1; __utmb=253913303.2.10.1521189299; ASP.NET_SessionId=ve5jy43znyiewowmrhl5zs3b; .UMSFORMSAUTH=186C670FBB00A7C983182C298375BD362DB4619C33C5DFB2594D435A21C59088FC52ED58A7C3907123A9B89883E9F58A66013085CB96895AF5E773242CA2FC7ABDF9E68AAD7385118C19CF77A68E803CCD13F9C9924A99DAD419749AFD4519620E234D22A5B13A641A5155EFA4D1B05B93258CA1EF0AD6E70AA1F0BF091F11F0'
    };

    var dataString = 'ScriptManager1=RadAjaxPanel1Panel%7CStartSearch&__EVENTTARGET=StartSearch&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=CZKsGagRCItN0plXaAc5RDMYFA8MG2HdrJIknX5A3r8zLINf8qVHPvDDPJD6CeDDHQDmxweRWqw6XauFQ4VYaAMujX7plSnxtbtThG%2FQZFuXSZET%2BjSaM9odn6PIHFbCZ6ktugJWzafSFoozPI25RcmsWosy7p06shdhUPYB9unNPbIWx5lAH1RwvlWm9EMcAVNsRx2f2aOcWtpLSn0eGYK5sVokl95i5umWBpN6ba7gn1iZSapeRiPKMfVLHb04PM1vwgjlUAmjvV7B4QMEF69hLsSPhNL3jJ61GZOV2kmanoW5Sk2qWPYTzi%2BG1WNKMp%2BwMIt1SArHJjzE6hDK6R2TpWWmdjYF%2Fxlpc6LWHcbsKRkI3N6EI1LZEoCsVtcM57IwmN2dUk102rT4LLZv499B%2FCnyCVpVtck0ZDs8Bat0IWELcBuyfloYXdjm%2FGXmlehhrnQtW%2BxnepAx%2BFSjJiuLoORdZOfwfeTaYBwRYIKAJLLFrzK%2BrWn1xJ3alRXKw2Y4hcqqTPCb0q1t2OYNgyDSfkd8rtGxAHqJX6c1pw6khNZCFLEhXPv18zjJVWIwyokr%2F5siIJmjg%2FYdSxvqRz9KCLLfpf9myDoW%2F1PdmxULrsibmfIkZAEceMmsPgffMtT8lTaXXO%2BB4PdUMrN0%2B2FBTGJJW3loLHGI2R3qC42s%2FN57lCDKEDL1jm3egoPUS%2BtknRdm7kNW1SZEZA9OZfl7eONKa5MEyJ46ccP2cvUFdkXfMuB1FB0D04VSCOzuXdaLCrMm1DjEh2CZci6DYZytqJRJ%2FzxEiuJVQuMCGXdiuu3CZRChIsOjCLLt%2B%2BnNnEJXkuqK3Ot19LqhujjZiU1kQdd9z81r6z9nbKWq0CZ%2F1KOgujyhrah4O7di0sZZan2brtEWi%2F8OMlMgui0wqYktVyn0t4PNHbm4HMNK4ib7t4%2BlEmTGCJ77hZLAS7AT4t445zJi18q1J%2Ba75fJFezXEXuusPaidqZtcMRtJwtdFfds81mKXGEckluIRLC6XNy1FDidxzDOPCXykJemmMCbpmAEVNJv7J2cmhMBZlf1VhgmOEklonqpXBGgpmYjbfjEJYuBeqny00hNLPdkdWE4nri8pWCYM290yFEUb3zq2HP7g1eIDLm5KeJlEIrlh%2BLfdB%2Bx86HAcPNkoZn0RoUek%2BeOqIpdQdaQCPqm6L7ohwZgPLwXBflRc9%2FZQQHM7HbSJGBnlb2GbezBuhT%2FjfqRabJ0KkvfQJD1at707nwCohGB0HO8L3UzswBLcEMYYvq0wARxony93B%2FdH9eCZpHwaWEoWTJhVUHpdrrNs38vao6PWZb25lpspgzgY%2B7LMgWwVEeFF6s%2FflpTn1ebn1nDvNb6aqhFDG3ni3Qs2T1NMsyT2IqJhIaOmcZ05Ls8eW7MLbL%2BVAwxD%2BSraX6jnNr1fpIn%2B4Pfr5XG0xky031o5YhA%2FqJOLTO3XjjkWt2fNVPTiCQBddKxu92HDxY4Th3x75ODElPICm85NnHkOqu4%2Fi3OGprFcT7fnhtoKIizJ%2BOtz0NmyFzabvgKIki7AVf%2BmclpeRcRnAVwltOo79KxUor%2FHU5ogUTDyTO21JsaKLuY9e1lmlfZMbojHPA%2FlQiSZ%2BAUVO8UAv31y4oEThDjK5vvIZCGF7PysaYEY%2FUFto1OX4DRY1zTUG%2B6ctQJulrKwBddm7QQnG%2B5SfbAAU1BEWQodFSt1DGc60aZ2%2B22RWEVLhK%2F9RpEfYKLuaBXmY80ITzNX9dTeMBYfu%2BYrQ1ZmE%2BVuXdFv3u2QDueAeHNSGE%2F%2F3nz1iSLRjvfNzn%2FXqPy0tW7zRRGjtuWwCHPF86J1IPvAQwXLvKG%2FJWHVVQmHhQ6KohJqJzcdmQrvK8%2BU4vCqk%2BlJI4PRtPeBTtKK%2FSDvBbJUXDlqnKG2M20c4%2FQ9aUXnAcRU0EoGdWKCQV6vSpemku7d6fcamnNtTilTdPHsULqkUHYN8Nub8%2B2OKxiXvP9LRQgaQw5BR%2F9l4NRqnkyTP4o%2FqA0dzeA1IZrP3UlSK2u4eLu6yCMB2ZEYexF5MBhmFv16zEEBUwXeK7wUU3l31kaI%2FgaBW6qF0z4EdESu%2F%2FFRMehp%2BfQ3pwD%2BZ8zNftqjjs0M%2B4ECtDrVdR%2Bq1HlehjC8NWAqUf3s3e8jU7giAvNiKrOOydf7isyIMY7lpptD6uTWfgxkmELutn1EJkyeBT4oeRQtoFk0feR15VTz6JujttOvfejR7KtLH2J8CSuNHX5LnXGW49aeQUc475HBt8Ru%2FmU%2BzpZMkQPDk4mPF%2Fvq3s9YbdvuPKtkwbLQuXIb%2BoB3KRR67zlcXSPOamO1BFdfjM3wlFNJE0lD2uNulvLWOPbie4O47zrdr9F4W5WUrHNus0N0J3XuB4%2BAUa5srYBohiNSxMweFPmpSNRw7WSsNymXoW3eLkiCUo%2FxCzg2ypjS8qlFwwP%2FifnxtTwsUvmxrRQg6uTFBQ86uSuQ08U%2B4iDWxZsj6M46jpfa9bpY1h3OOjYK5%2FXtRKLxplsq4X0edl2aVxVvaXGa3s0s9StiwSsY%2BxSemmQFU6TAkLlEGMeuehiX%2BGki4I897p83An1jqwusg9DcgbWw4OF3YZikVKvDTETOX8b0iCLSW7W4FJsqf%2FG4hQ3Y9wc3zdDHAj%2BRI25kg6M36beaP3hY7Ssi1Plv30NAQqrkwubUHioXVBuYfIlOaPpPNEWByUOhgSsCWJUuT%2Fcrn7CjrXj5qkm4bqPW7Eoq3ZFJqPoncIcGQeTjyacqGiih68IuXDZB38UIM8bwOC0W05kLnfZogMnciVkiJEcw28yrOfutaZhZtuyVViSev7C%2BYkfvzkwgdgwMNI82IcrWm8uYvJI%3D&__VIEWSTATEGENERATOR=CF2578C7&Login='+login+'&DisplayName=Jasmin%20Bom&Domain='+domain+'&Type='+type+'&SearchPrefix=&ShowActions=on&currentViewType=SchemaView&currentViewDate=16-03-2018&ClickAction=&txtDivCalendarWidth=&SearchText=Jasmin%20Bom&SearchOption=all%7C0%7C1&AppointmentID=&timetableblockid=&timetableadminserver=&RadHomeWork%24C%24txtHomework=&RadHomeWork_ClientState=&RadComments%24C%24txtComments=&RadComments_ClientState=&hiddenInputToUpdateATBuffer_CommonToolkitScripts=1&__ASYNCPOST=true&RadAJAXControlID=RadAjaxPanel1';

    var options = {
        url: 'https://selvbetjening.aarhustech.dk/WebTimeTable/default.aspx',
        method: 'POST',
        headers: headers,
        body: dataString
    };

    function innercallbacj(error, response, body) {
        if (!error && response.statusCode == 200) {
            console.log('')
            if (body === '1|#||4|76|pageRedirect||%2fUMSLogin%2fweblogin.aspx%3fReturnUrl%3d%252fWebTimeTable%252fdefault.aspx|') {
                dologin(function() {
                    request(options, innercallbacj);
                });
            } else {
                callback(body);
            }
        }
    }

    request(options, innercallbacj);
    
}


timetable('S', 'oliv5179', 'edu.ats.dk', function(x) {console.log(x)});