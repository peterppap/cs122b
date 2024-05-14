function handleLinkResult(resultData) {
    console.log("handleLinkResult: ");
    let genreLink = jQuery("#genreLink");
    for (let i = 0; i < resultData.length; i++){
        if (i % 4 === 0 && i !== 0) {
            genreLink.append('<br>');
        }
        var genre = resultData[i]["genre"];
        var genreId = resultData[i]['genreId'];
        genreLink.append('<a href="result-browse.html?genre=' + genreId + '&page=1' + '">' + genre + '</a>');
    }
    let prefixLink = jQuery("#prefixLink");
    const alph = [...Array(26)].map((_, i) => String.fromCharCode(65 + i));
    for (let i = 0; i < alph.length; i++) {
        var a = alph[i];
        prefixLink.append('<a href="result-browse.html?prefix=' + a + '&page=1' + '">' + a + " "+ '</a>');
    }
    let prefix0Link = jQuery("#prefix0link");
    for (let i = 0; i < 10; i++) {
        prefix0Link.append('<a href="result-browse.html?prefix=' + i + '&page=1' + '">' + i + " " + '</a>');
    }
    prefix0Link.append('<a href="result-browse.html?prefix=' + '*' + '&page=1' + '">' + '*' + '</a>');
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/searchMain",
    success: (resultData) => handleLinkResult(resultData)
});