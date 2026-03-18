create table diet_restriction
(
    id   bigint auto_increment comment '忌口id'
        primary key,
    name varchar(30) not null comment '忌口名称',
    constraint diet_restriction_pk_2
        unique (name)
)
    comment '忌口表';

create table disease
(
    id   bigint auto_increment comment '疾病ID（自增主键）'
        primary key,
    name varchar(50) not null comment '疾病名称（如：感冒、肠胃炎、高血压）',
    constraint uk_name
        unique (name)
)
    comment '疾病字典表（全局公共）';

create table snack_nutrition
(
    id            int auto_increment comment '主键ID，自增唯一标识'
        primary key,
    snack_name    varchar(255)                       not null comment '零食名称，如：原味薯片、牛奶巧克力、手撕面包',
    energy        decimal(8, 2)                      not null comment '能量，单位：千焦(kJ)【食品标签通用单位】',
    protein       decimal(6, 2)                      null comment '蛋白质含量，单位：克(g)/100克',
    fat           decimal(6, 2)                      null comment '脂肪含量，单位：克(g)/100克',
    fat_saturated decimal(6, 2)                      null comment '饱和脂肪，单位：克(g)/100克（可选，常见于加工零食）',
    carbohydrate  decimal(6, 2)                      null comment '碳水化合物，单位：克(g)/100克',
    sugar         decimal(6, 2)                      null comment '糖含量，单位：克(g)/100克（含添加糖+天然糖）',
    sodium        int                                null comment '钠含量，单位：毫克(mg)/100克（食品标签通用低单位）',
    dietary_fiber decimal(6, 2)                      null comment '膳食纤维，单位：克(g)/100克（粗粮/代餐零食常用）',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间，自动生成',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间，修改时自动刷新',
    constraint uk_snack_name
        unique (snack_name) comment '零食名称唯一索引，避免重复录入同款零食'
)
    comment '零食营养数据表' collate = utf8mb3_general_ci;

create index idx_update_time
    on snack_nutrition (update_time)
    comment '更新时间索引，方便按时间筛选最新营养数据';

create table user
(
    id          bigint auto_increment comment '用户id'
        primary key,
    username    varchar(50)  not null comment '用户名',
    password    varchar(200) not null comment '密码',
    email       varchar(50)  not null comment '邮箱',
    create_time datetime     not null comment '创建时间',
    avatar_url  varchar(512) not null comment '头像图片地址'
)
    comment '用户表';

create table agent_lib
(
    agent_id       int auto_increment comment '智能体主键ID'
        primary key,
    agent_name     varchar(100)                       not null comment '智能体名称（唯一）',
    create_user_id bigint                             not null comment '创建者ID，外键关联用户表',
    prompt         text                               not null comment '智能体核心提示词（长文本）',
    data_file_url  varchar(512)                       null comment '关联数据文件地址URL，多文件用英文逗号分隔',
    agent_desc     varchar(255)                       null comment '智能体功能描述/适用场景',
    status         tinyint  default 1                 not null comment '1-启用 0-禁用 2-待审核',
    create_time    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_agent_name
        unique (agent_name),
    constraint fk_agent_user
        foreign key (create_user_id) references user (id)
            on delete cascade
)
    comment '智能体库表';

create index idx_create_user_id
    on agent_lib (create_user_id);

create index idx_status
    on agent_lib (status);

create table ai_conversation
(
    id              bigint auto_increment comment '对话ID'
        primary key,
    user_id         bigint                             not null comment '所属用户ID',
    title           varchar(100)                       not null comment '对话标题',
    context_summary text                               null comment '对话摘要',
    status          tinyint  default 1                 not null comment '状态：0-已结束，1-进行中',
    create_time     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime                           null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_flag     tinyint  default 0                 not null comment '软删除：0-未删，1-已删',
    constraint fk_conversation_user
        foreign key (user_id) references user (id)
)
    comment 'AI对话表';

create index idx_create_time
    on ai_conversation (create_time);

create index idx_user_id
    on ai_conversation (user_id);

create table ai_message
(
    id              bigint auto_increment comment '消息ID'
        primary key,
    conversation_id bigint                             not null comment '所属对话ID',
    user_id         bigint                             not null comment '所属用户ID（冗余）',
    content         text                               not null comment '消息内容',
    sequence        int                                not null comment '对话内消息序号',
    function_type   tinyint  default 0                 null comment '功能选择：0-AI对话，1-三餐分析。2-零食分析，3-报告识别',
    create_time     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    delete_flag     tinyint  default 0                 not null comment '软删除：0-未删，1-已删',
    role            varchar(20)                        not null comment '角色：user/assistant/system',
    img_url         varchar(50)                        null comment '图片地址',
    constraint fk_message_conversation
        foreign key (conversation_id) references ai_conversation (id),
    constraint fk_message_user
        foreign key (user_id) references user (id)
)
    comment 'AI消息表';

create index idx_conversation_id
    on ai_message (conversation_id);

create index idx_conversation_seq
    on ai_message (conversation_id, sequence);

create index idx_user_id
    on ai_message (user_id);

create table user_diet_restriction_rel
(
    user_id        bigint not null comment '用户id',
    restriction_id bigint not null comment '忌口id',
    primary key (user_id, restriction_id),
    constraint user_diet_restriction_rel_diet_restriction_id_fk
        foreign key (restriction_id) references diet_restriction (id),
    constraint user_diet_restriction_rel_user_id_fk
        foreign key (user_id) references user (id)
)
    comment '用户忌口关联表';

create table user_disease_rel
(
    user_id    bigint not null comment '关联用户表ID',
    disease_id bigint not null comment '关联疾病字典表ID',
    primary key (user_id, disease_id),
    constraint user_disease_rel_disease_id_fk
        foreign key (disease_id) references disease (id)
            on update cascade on delete cascade,
    constraint user_disease_rel_sys_user_id_fk
        foreign key (user_id) references user (id)
            on update cascade on delete cascade
)
    comment '用户当前疾病关联表（多对多，统计正在生的病）';

create table user_medicine
(
    medicine_id   bigint auto_increment comment '药品记录主键ID'
        primary key,
    user_id       bigint                             not null comment '关联用户ID，外键关联user表',
    medicine_name varchar(100)                       not null comment '药品名称',
    take_times    tinyint  default 1                 not null comment '每日服用次数1/2/3',
    single_dosage bigint   default 1                 not null comment '单次服用数量',
    stop_time     datetime                           null comment '停药时间（精确到时分）',
    status        tinyint  default 1                 not null comment '1-正在服用 0-已停药',
    create_time   datetime default CURRENT_TIMESTAMP null comment '记录创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '记录更新时间',
    constraint fk_medicine_user
        foreign key (user_id) references user (id)
            on delete cascade
)
    comment '用户药品服用记录表';

create index idx_user_id
    on user_medicine (user_id);

create table user_snack_record
(
    id                bigint auto_increment comment '主键ID，自增唯一标识'
        primary key,
    user_id           bigint                                 not null comment '用户ID，关联用户表主键，和三餐/零食表保持一致',
    role              tinyint(1)                             not null comment '饮品==0，袋装零食==1',
    snack_record_name varchar(255) default ''                not null comment '零食记录备注名，如：下午加餐薯片、追剧吃坚果（自定义）',
    snack_id          bigint                                 null comment '关联零食营养表主键ID，通用零食填此值，自定义零食留NULL',
    count             double       default 0                 not null comment '食用量，单位：克(g/ml)（推荐），支持小数如25.5g，精准统计',
    create_time       datetime     default CURRENT_TIMESTAMP not null comment '记录创建时间，自动生成',
    update_time       datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '记录更新时间，修改自动刷新'
)
    comment '用户零食食用记录表' collate = utf8mb3_general_ci;

create index idx_snack_id
    on user_snack_record (snack_id)
    comment '零食ID索引，提升联表查营养效率';

create index idx_user_id
    on user_snack_record (user_id)
    comment '用户ID索引，提升按用户查询效率';

create index idx_user_snack
    on user_snack_record (user_id, snack_id)
    comment '联合索引，适配「用户+零食」高频查询';

create table user_three_meals
(
    id           bigint auto_increment comment '主键ID，自增唯一标识'
        primary key,
    user_id      bigint                                 not null comment '用户ID，关联用户表的主键',
    meal_type    tinyint(1)                             not null comment '餐食类型：1=早餐，2=午餐，3=晚餐',
    meal_name    varchar(255)                           not null comment '餐食名称，如：鸡蛋牛奶+全麦面包、番茄牛腩饭',
    meal_pic_url varchar(512) default ''                null comment '餐食图片URL，可为空（无图片时存空字符串）',
    ai_suggest   text                                   null comment 'AI饮食建议，支持长文本，可为空',
    update_time  datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间，自动生成/更新'
)
    comment '用户三餐记录表';

create index idx_user_id
    on user_three_meals (user_id)
    comment '用户ID索引，提升按用户查询效率';

create index idx_user_meal_time
    on user_three_meals (user_id, meal_type, update_time)
    comment '联合索引，适配「用户+餐型+时间」的高频查询';

