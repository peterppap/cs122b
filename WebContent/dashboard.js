function handleMetaResult(resultData) {
    let metadataContainer = jQuery("#metadataContainer");

    for (const [tableName, columns] of Object.entries(resultData)) {
        let tableMetadata = '<h2>' + tableName + ':</h2><tr>';

        for (const column of columns) {
            tableMetadata += '<h5>' + column + '</h5>';
            tableMetadata += '<br>';
        }

        tableMetadata += '</tr>';
        metadataContainer.append(tableMetadata);
    }
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/meta",
    success: (resultData) => handleMetaResult(resultData)
});