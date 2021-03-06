function printClustersHTML(r, dataset, path, errorMin, beginFrom)

fid = fopen(strcat(path,'/HTML/clusters_',beginFrom,'/clusters.htm'), 'w');

html = ['<html>'...
'<head>'...
'<title>Multimodal summarization (, error = )</title>'...
'</head>'...
'<body>'...
'<table width="800" border="0"><tr><td>Training</td><td>Cluster</td>'];

for i=1:r
    html = strcat(html, '<td><a href="c', num2str(i), '.htm" target="_blank">', num2str(i),'</td>');
end

html = strcat(html,'<table><td><a href="testing.htm">Validation</a><td></table>');
html = strcat(html,'</tr></table></body></html>');

fprintf(fid, html);
fclose(fid);
