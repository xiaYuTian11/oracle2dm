# oracle2dm 

## 变更长度
```sql
alter table DAIMA modify YDBM VARCHAR2(8000);
alter table DAIMA modify DWMC VARCHAR2(8000);
alter table ZJ_WJYS modify WJMC VARCHAR2(8000);
alter table DFPBZLX modify DFPBZLX_DESP VARCHAR2(8000);
alter table GBP_USER_BEFORMD5 modify REALNAME VARCHAR2(8000);
alter table JJJ modify YDBM VARCHAR2(8000);
alter table LOCAL_BD modify BEIZHU VARCHAR2(8000);
alter table LXFS modify LXFS_LXR VARCHAR2(8000);
alter table REPORT_JGSY_COMMON modify JGSY_NAME VARCHAR2(8000);
alter table REPORT_SY modify INCORPORATOR VARCHAR2(8000);
alter table REPORT_SY modify JGBZWH VARCHAR2(8000);
alter table REPORT_T_BWRY_JBXX modify XM VARCHAR2(8000);
alter table SY modify INCORPORATOR VARCHAR2(8000);
alter table ZHXSH modify DESP VARCHAR2(8000);

--  JDXZ_BD表中的BEIZHU字段需要手动更改为text类型
```