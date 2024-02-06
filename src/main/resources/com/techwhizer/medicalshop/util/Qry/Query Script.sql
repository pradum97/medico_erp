/* DATABASE DETAILS

        DB_USERNAME =postgres
        DB_PASSWORD =postgres
        DB_PORT = 5432
        DB_NAME = medical_shop
*/

/*drop table if exists tbl_cart cascade;
drop table if exists tbl_company cascade;
drop table if exists tbl_dealer cascade;
drop table if exists tbl_discount cascade;
drop table if exists tbl_doctor cascade;
drop table if exists tbl_items_master cascade;
drop table if exists tbl_license cascade;
drop table if exists tbl_manufacturer_list cascade;
drop table if exists tbl_mr_list cascade;
drop table if exists tbl_patient cascade;
drop table if exists TBL_PRODUCT_TAX cascade;
drop table if exists tbl_purchase_items cascade;
drop table if exists tbl_purchase_main cascade;
drop table if exists tbl_role cascade;
drop table if exists tbl_sale_items cascade;
drop table if exists tbl_sale_main cascade;
drop table if exists tbl_shop_details cascade;
drop table if exists tbl_stock cascade;
drop table if exists tbl_users cascade;
drop table if exists TBL_RETURN_ITEMS cascade;
drop table if exists TBL_RETURN_MAIN cascade;
drop table if exists tbl_frequency cascade;
drop table if exists TBL_MEDICINE_TIME cascade;

drop table if exists TBL_SALUTATION cascade;
drop table if exists patient_consultation cascade;
drop table if exists PRESCRIBE_MEDICINE_MASTER cascade;
drop table if exists PRESCRIBE_MEDICINE_ITEMS cascade;
drop table if exists payment_information cascade;
*/
CREATE TABLE tbl_users
(
    USER_ID        SERIAL PRIMARY KEY       NOT NULL,
    FIRST_NAME     VARCHAR(50)              NOT NULL,
    LAST_NAME      VARCHAR(50),
    USERNAME       VARCHAR(100)             NOT NULL UNIQUE,
    GENDER         VARCHAR(6)               NOT NULL,
    role_id        integer      default 1   NOT NULL,
    EMAIL          VARCHAR(100) UNIQUE      NOT NULL,
    PHONE          bigint UNIQUE            NOT NULL,
    PASSWORD       VARCHAR(200)             NOT NULL,
    FULL_ADDRESS   VARCHAR(200) default null,
    USER_IMG_PATH  TEXT         default 'avtar_3.png',
    CREATED_TIME   timestamp    DEFAULT CURRENT_TIMESTAMP,
    ACCOUNT_STATUS int          default '1' NOT NULL,
    MAC_ADDRESS    VARCHAR(30)
);

/*CREATE ADMIN*/
INSERT INTO tbl_users(first_name, last_name, username, gender, email, phone, password)
VALUES ('Admin', 'Account', 'admin', 'MALE', 'pradumraj98@gmail.com', 9608461591,
        '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918');

CREATE TABLE TBL_SHOP_DETAILS
(
    shop_id           serial primary key  not null,
    SHOP_NAME         VARCHAR(100) unique NOT NULL,
    SHOP_EMAIL        VARCHAR(100) unique NOT NULL,
    SHOP_PHONE_1      VARCHAR(40) unique  NOT NULL,
    SHOP_PHONE_2      VARCHAR(40),
    SHOP_GST_NUMBER   VARCHAR(100),
    SHOP_FOOD_LICENCE VARCHAR(100),
    SHOP_DRUG_LICENCE VARCHAR(100),
    SHOP_ADDRESS      VARCHAR(200) unique NOT NULL
);

CREATE TABLE TBL_PRODUCT_TAX
(
    TAX_ID      SERIAL PRIMARY KEY NOT NULL,
    SGST        NUMERIC            NOT NULL,
    CGST        NUMERIC            NOT NULL,
    IGST        NUMERIC            NOT NULL,
    gstName     VARCHAR(100),
    HSN_SAC     NUMERIC UNIQUE     not null,
    DESCRIPTION VARCHAR(200)
);

CREATE TABLE TBL_ROLE
(
    ROLE_ID SERIAL PRIMARY KEY NOT NULL,
    ROLE    VARCHAR(50) unique
);

INSERT INTO TBL_ROLE (ROLE)
VALUES ('ADMIN'),
       ('STAFF'),
       ('DOCTOR');

CREATE TABLE tbl_dealer
(
    dealer_id    SERIAL PRIMARY KEY,
    dealer_name  varchar(100) not null,
    dealer_phone varchar(20)  not null,
    dealer_email varchar(100),
    dealer_dl    varchar(100),
    dealer_gstNo varchar(100),
    ADDRESS      TEXT         NOT NULL,
    STATE        VARCHAR(20),
    ADDED_DATE   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tbl_company
(
    company_id      serial primary key,
    company_name    varchar(200) not null,
    company_address varchar(200) not null,
    created_date    timestamp default current_timestamp
);

CREATE TABLE TBL_DISCOUNT
(

    DISCOUNT_ID   SERIAL PRIMARY KEY NOT NULL,
    DISCOUNT      NUMERIC            NOT NULL,
    DISCOUNT_NAME VARCHAR(200)       not null,
    description   varchar(200),
    created_date  timestamp default current_timestamp

);

CREATE TABLE tbl_manufacturer_list
(
    mfr_id            serial primary key,
    manufacturer_name varchar(200),
    created_date      timestamp default current_timestamp
);
CREATE TABLE tbl_mr_list
(
    mr_id        serial primary key,
    name         varchar(200) not null,
    phone        varchar(20),
    gender       varchar(20)  not null,
    email        varchar(100),
    company      varchar(100),
    address      varchar(200),
    created_date timestamp default current_timestamp
);

CREATE TABLE TBL_ITEMS_MASTER
(
    ITEM_ID      SERIAL PRIMARY KEY,
    ITEMS_NAME   VARCHAR(300) NOT NULL,
    UNIT         VARCHAR(100) NOT NULL,
    STRIP_TAB    NUMERIC   default 0,
    PACKING      VARCHAR(100) NOT NULL,
    COMPANY_ID   INT,
    MFR_ID       INT,
    DISCOUNT_ID  INT,
    MR_ID        INT,
    GST_ID       INT          NOT NULL,
    TYPE         VARCHAR(50)  NOT NULL,
    NARCOTIC     VARCHAR(50)  NOT NULL,
    ITEM_TYPE    VARCHAR(50)  NOT NULL,
    STATUS       INT          NOT NULL,
    CREATED_BY   INT,
    CREATED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    IS_ACTIVE    INT       DEFAULT 1,
    COMPOSITION  VARCHAR(300) NOT NULL,
    DOSE         VARCHAR(200) NOT NULL,
    TAG          VARCHAR(300) NOT NULL,
    is_stockable boolean,
    foreign key (GST_ID) REFERENCES tbl_product_tax (TAX_ID),
    foreign key (CREATED_BY) REFERENCES tbl_users (user_id),
    foreign key (MFR_ID) REFERENCES tbl_manufacturer_list (MFR_ID),
    foreign key (MR_ID) REFERENCES tbl_mr_list (MR_ID)
);

CREATE TABLE TBL_PURCHASE_MAIN
(
    PURCHASE_MAIN_ID SERIAL PRIMARY KEY,
    DEALER_ID        INT         ,
    BILL_NUM         VARCHAR(50) NOT NULL,
    DEALER_BILL_NUM  VARCHAR(50),
    BILL_DATE        VARCHAR(15) NOT NULL,
    CREATED_DATE     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    IS_ACTIVE        INT       DEFAULT 1
);

CREATE TABLE TBL_PURCHASE_ITEMS
(
    PURCHASE_ITEMS_ID SERIAL PRIMARY KEY,
    PURCHASE_MAIN_ID  INT          NOT NULL,
    ITEM_ID           INT          NOT NULL,
    BATCH             VARCHAR(100) ,
    EXPIRY_DATE       VARCHAR(50) ,
    LOT_NUMBER        VARCHAR(50),
    PURCHASE_RATE     NUMERIC,
    MRP               NUMERIC,
    SALE_PRICE        NUMERIC,
    QUANTITY          NUMERIC      NOT NULL,
    QUANTITY_UNIT     VARCHAR(20)  NOT NULL,
    FOREIGN KEY (PURCHASE_MAIN_ID) REFERENCES TBL_PURCHASE_MAIN (PURCHASE_MAIN_ID),
    FOREIGN KEY (ITEM_ID) REFERENCES TBL_ITEMS_MASTER (ITEM_ID)
);

CREATE TABLE TBL_STOCK
(
    STOCK_ID          SERIAL PRIMARY KEY,
    ITEM_ID           INT         NOT NULL,
    PURCHASE_MAIN_ID  INT         NOT NULL,
    PURCHASE_ITEMS_ID INT         NOT NULL,
    QUANTITY          NUMERIC     NOT NULL,
    QUANTITY_UNIT     VARCHAR(20) NOT NULL,
    UPDATE_DATE       VARCHAR(20) NOT NULL,
    FOREIGN KEY (ITEM_ID) REFERENCES TBL_ITEMS_MASTER (ITEM_ID),
    FOREIGN KEY (PURCHASE_MAIN_ID) REFERENCES TBL_PURCHASE_MAIN (PURCHASE_MAIN_ID),
    FOREIGN KEY (PURCHASE_ITEMS_ID) REFERENCES TBL_PURCHASE_ITEMS (PURCHASE_ITEMS_ID)
);

CREATE TABLE TBL_PATIENT
(
    PATIENT_ID         SERIAL PRIMARY KEY NOT NULL,

    NAME               VARCHAR(100)       NOT NULL,
    PHONE              VARCHAR(30),
    ADDRESS            VARCHAR(200),
    ID_NUMBER          VARCHAR(100),
    GENDER             VARCHAR(10),
    AGE                VARCHAR(5),
    CARE_OF            VARCHAR(100),
    WEIGHT             VARCHAR(50),
    BP                 VARCHAR(50),
    PULSE              VARCHAR(50),
    SUGAR              VARCHAR(50),
    SPO2               VARCHAR(100),
    TEMP               VARCHAR(100),
    CVS                VARCHAR(100),
    CNS                VARCHAR(100),
    CHEST              VARCHAR(100),
    invoice_number     varchar(50),
    created_by         int,
    last_update        timestamp,
    last_update_by     int,
    REFER_BY           VARCHAR(200),
    consultant_dr_name VARCHAR(300),
    registered_date    timestamp default CURRENT_TIMESTAMP
);

CREATE TABLE TBL_CART
(
    CART_ID      BIGSERIAL PRIMARY KEY NOT NULL,
    stock_id     INTEGER               NOT NULL,
    MRP          NUMERIC               NOT NULL,
    STRIP        INT,
    PCS          INT,
    created_by int not null,
    CREATED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TBL_SALE_MAIN
(
    SALE_MAIN_ID                   SERIAL PRIMARY KEY                  NOT NULL,
    PATIENT_ID                     INTEGER                             NOT NULL,
    DOCTOR_ID                      INT,
    SELLER_ID                      INTEGER                             NOT NULL,
    ADDITIONAL_DISCOUNT_AMOUNT     NUMERIC,
    ADDITIONAL_DISCOUNT_PERCENTAGE NUMERIC,
    TOTAL_DISCOUNT_AMOUNT          NUMERIC,
    PAYMENT_MODE                   VARCHAR                             NOT NULL,
    TOT_TAX_AMOUNT                 NUMERIC,
    NET_AMOUNT                     NUMERIC                             NOT NULL,
    INVOICE_NUMBER                 VARCHAR(100)                        NOT NULL,
    BILL_TYPE                      VARCHAR(100)                        NOT NULL,
    sale_date                      timestamp default CURRENT_TIMESTAMP NOT NULL,
    RECEIVED_AMOUNT                NUMERIC,
    FOREIGN KEY (SELLER_ID)
        REFERENCES tbl_users (user_id)
);

CREATE TABLE TBL_SALE_ITEMS
(
    SALE_ITEM_ID                   BIGSERIAL PRIMARY KEY               NOT NULL,
    SALE_MAIN_ID                   INTEGER                             NOT NULL,
    ITEM_ID                        INTEGER                             NOT NULL,
    ITEM_NAME                      VARCHAR(200)                        NOT NULL,

    PACK                           VARCHAR(200),
    MFR_ID                         INT,
    BATCH                          VARCHAR(200),
    EXPIRY_DATE                    VARCHAR(50),

    PURCHASE_RATE                  NUMERIC                             NOT NULL,
    MRP                            NUMERIC   DEFAULT 0                 NOT NULL,
    SALE_RATE                      NUMERIC                             NOT NULL,
    STRIP                          INT       default 0,
    PCS                            INT       default 0,
    DISCOUNT                       numeric,
    HSN_SAC                        NUMERIC,
    igst                           NUMERIC,
    sgst                           NUMERIC,
    cgst                           NUMERIC,
    STRIP_TAB                      INT,
    NET_AMOUNT                     NUMERIC                             NOT NULL,
    TAX_AMOUNT                     NUMERIC,
    sale_date                      timestamp default CURRENT_TIMESTAMP NOT NULL,
    stock_id                       integer                             not null,
    is_stockable boolean,
    ADDITIONAL_DISCOUNT_PERCENTAGE numeric DEFAULT 0,

    FOREIGN KEY (SALE_MAIN_ID)
        REFERENCES TBL_SALE_MAIN (SALE_MAIN_ID),
    FOREIGN KEY (ITEM_ID)
        REFERENCES tbl_items_master (item_id)
);

CREATE TABLE TBL_DOCTOR
(
    DOCTOR_ID     SERIAL PRIMARY KEY,
    DR_NAME       VARCHAR(100) NOT NULL,
    DR_PHONE      varchar(20),
    DR_ADDRESS    VARCHAR(200),
    DR_REG_NUM    VARCHAR(100),
    SPECIALITY    VARCHAR(100),
    QUALIFICATION VARCHAR(300),
    CREATED_DATE  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TBL_LICENSE
(
    LICENSE_ID          SERIAL PRIMARY KEY,
    COMPANY_ID          INT unique          NOT NULL,
    APPLICATION_ID      VARCHAR(50) unique  NOT NULL,
    SERIAL_KEY          VARCHAR(100) unique NOT NULL,
    START_ON            VARCHAR(20)         NOT NULL,
    EXPIRES_ON          VARCHAR(20)         NOT NULL,
    LICENSE_TYPE        VARCHAR(50)         NOT NULL,
    LICENSE_PERIOD_DAYS INTEGER             NOT NULL,
    REGISTERED_EMAIL    VARCHAR(100)        NOT NULL
);

CREATE TABLE TBL_RETURN_MAIN
(
    RETURN_MAIN_ID SERIAL PRIMARY KEY,
    INVOICE_NUMBER VARCHAR(100),
    RETURN_BY_ID   INT          NOT NULL,
    REFUND_AMOUNT  NUMERIC      NOT NULL,
    REMARK         VARCHAR(500) NULL,
    FOREIGN KEY (RETURN_BY_ID) REFERENCES tbl_users (user_id),
    RETURN_DATE    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TBL_RETURN_ITEMS
(
    RETURNS_ID      SERIAL PRIMARY KEY,
    STOCK_ID        INT NOT NULL,
    SALE_ITEM_ID    INT NOT NULL,
    QUANTITY        BIGINT,
    QUANTITY_UNIT   VARCHAR(20),
    RETURN_MAIN_ID  INT NOT NULL,
    DISCOUNT_AMOUNT numeric,
    AMOUNT          numeric,
    NET_AMOUNT      numeric
);

CREATE TABLE tbl_frequency
(
    frequency_ID SERIAL PRIMARY KEY,
    frequency    VARCHAR(200),
    CREATION_ID  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY   INT
);


CREATE TABLE TBL_MEDICINE_TIME
(
    medicine_time_ID SERIAL PRIMARY KEY,
    time             VARCHAR(200),
    CREATION_ID      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY       INT
);

CREATE TABLE TBL_SALUTATION
(
    SALUTATION_ID SERIAL PRIMARY KEY,
    NAME          VARCHAR(50) NOT NULL,
    CREATED_DATE  timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TBL_PATIENT
(
    PATIENT_ID       SERIAL PRIMARY KEY NOT NULL,
    SALUTATION_ID    INT,
    FIRST_NAME       VARCHAR(100)       NOT NULL,
    PATIENT_CATEGORY VARCHAR(200) DEFAULT 'GENERAL/CASH',
    UHID_NO          VARCHAR(200),
    admission_number varchar(50),
    MIDDLE_NAME      VARCHAR(100),
    LAST_NAME        VARCHAR(100),
    GENDER           VARCHAR(10),
    ADDRESS          VARCHAR(200),
    DOB              VARCHAR(50),
    PHONE            VARCHAR(30),
    ID_TYPE          VARCHAR(200),
    ID_NUMBER        VARCHAR(100),
    guardian_name    VARCHAR(100),
    WEIGHT           VARCHAR(50),
    BP               VARCHAR(50),
    PULSE            VARCHAR(50),
    SUGAR            VARCHAR(50),
    SPO2             VARCHAR(100),
    TEMP             VARCHAR(100),
    CVS              VARCHAR(100),
    CNS              VARCHAR(100),
    CHEST            VARCHAR(100),
    created_by       int,
    last_update      timestamp,
    last_update_by   int,
    CREATION_DATE    timestamp    default CURRENT_TIMESTAMP
);


ALTER TABLE TBL_DOCTOR
    ADD COLUMN DOCTOR_TYPE VARCHAR(100);


CREATE TABLE patient_consultation
(
    consultation_id        SERIAL PRIMARY KEY,
    patient_id             INT,
    referred_by_doctor_id  INT,
    referred_by_name       varchar(300),
    description            varchar(1000),
    consultation_doctor_id INT,
    receipt_num            varchar(100),
    receipt_type           varchar(100),
    remarks                varchar(500),
    consultant_status      VARCHAR(100) DEFAULT 'Pending',
    consultation_date      timestamp    DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY             INT,
    CREATION_DATE          timestamp    default CURRENT_TIMESTAMP,
    UPDATE_BY              INT,
    LAST_UPDATE_DATE       timestamp    default CURRENT_TIMESTAMP
);

CREATE TABLE PRESCRIBE_MEDICINE_MASTER
(
    PRESCRIBE_MASTER_MEDICINE_ID SERIAL PRIMARY KEY,
    consultation_id              int,
    PATIENT_ID                   INT,
    INVOICE_NUM                  VARCHAR(30),
    CREATED_BY                   INT,
    CREATION_DATE                TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
;

CREATE TABLE PRESCRIBE_MEDICINE_ITEMS
(
    PRESCRIBE_ITEMS_MEDICINE_ID  SERIAL PRIMARY KEY,
    ITEM_NAME                    VARCHAR(400),
    ITEM_ID                      INT,
    PRESCRIBE_MASTER_MEDICINE_ID INT,
    IS_ITEM_EXISTS_IN_STOCK      bool,
    COMPOSITION                  VARCHAR(300),
    TAG                          VARCHAR(200),
    REMARK                       VARCHAR(1000),
    QUANTITY                     VARCHAR(100),
    TIME                         VARCHAR(500),
    DOSE                         VARCHAR(200),
    FREQUENCY                    VARCHAR(400),
    DURATION                     VARCHAR(100),
    STATUS                       VARCHAR(50) DEFAULT 1,
    CREATION_DATE                TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);



CREATE TABLE payment_information
(
    payment_id      SERIAL PRIMARY KEY,
    purpose         VARCHAR(255),
    DOCUMENT_SOURCE VARCHAR(300),
    DOCUMENT_ID     int,
    payment_date    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    amount          DECIMAL(10, 2)                        NOT NULL,
    payment_method  VARCHAR(50)                           NOT NULL,
    transaction_id  VARCHAR(100),
    contact_email   VARCHAR(100),
    contact_phone   VARCHAR(20),
    REMARKS         VARCHAR(500),
    payment_status  VARCHAR(20) DEFAULT 'SUCCESS',
    CREATION_DATE   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE consultation_setup
(
    consultation_setup_ID SERIAL PRIMARY KEY,
    consultation_fee      numeric not null,
    fee_valid_days        int     not null,
    CREATED_BY            INT     NOT NULL,
    CREATION_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

insert into consultation_setup(consultation_fee, fee_valid_days, CREATED_BY)
VALUES (400, 25, 1);

ALTER TABLE TBL_SALE_MAIN
    ADD COLUMN PAYMENT_REFERENCE_NUM VARCHAR(300),
    ADD COLUMN REMARKS               VARCHAR(500),
    ADD COLUMN CREATED_BY            INT;


CREATE  TABLE TBL_DUES(
    DUES_ID SERIAL PRIMARY KEY ,
    SOURCE_ID INT,
    DUES_TYPE VARCHAR(200),
    DUES_AMOUNT NUMERIC,
    CREATED_BY INT,
    CREATED_DATE timestamp DEFAULT CURRENT_TIMESTAMP
);









