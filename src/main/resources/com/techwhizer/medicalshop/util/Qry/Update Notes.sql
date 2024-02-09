alter table tbl_return_main add sale_main_id int;

update tbl_return_main set sale_main_id =170 where return_main_id =1;
