\timing;
select count(*) from ibobadm.psp_source_system_transmission;
select max(octet_length(response_document))/1024/1024 from ibobadm.psp_source_system_transmission;
select max(octet_length(request_document))/1024/1024 from ibobadm.psp_source_system_transmission;

select octet_length(request_document)/1024/1024,count(*) from ibobadm.psp_source_system_transmission
group by octet_length(request_document)/1024/1024
having octet_length(request_document)/1024/1024<30;
select octet_length(request_document)/1024/1024,count(*) from ibobadm.psp_source_system_transmission
group by octet_length(request_document)/1024/1024
having octet_length(request_document)/1024/1024 between 30 and 100;
select octet_length(request_document)/1024/1024,count(*) from ibobadm.psp_source_system_transmission
group by octet_length(request_document)/1024/1024
having octet_length(request_document)/1024/1024>100;


select octet_length(response_document)/1024/1024,count(*) from ibobadm.psp_source_system_transmission
group by octet_length(response_document)/1024/1024
having octet_length(response_document)/1024/1024<30;
select octet_length(response_document)/1024/1024,count(*) from ibobadm.psp_source_system_transmission
group by octet_length(response_document)/1024/1024
having octet_length(response_document)/1024/1024 between 30 and 100;
select octet_length(response_document)/1024/1024,count(*) from ibobadm.psp_source_system_transmission
group by octet_length(response_document)/1024/1024
having octet_length(response_document)/1024/1024>100;
