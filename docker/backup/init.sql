--
-- PostgreSQL database dump
--

\restrict mVX1ZeuwZbwXhcr34wNZbqntm3Manenfn6olAvjhieL1zcRC3fa0cVpWcZziABM

-- Dumped from database version 16.13 (Debian 16.13-1.pgdg12+1)
-- Dumped by pg_dump version 16.13 (Debian 16.13-1.pgdg12+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: fitness_user
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO fitness_user;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: fitness_user
--

COMMENT ON SCHEMA public IS '';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: vector; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA public;


--
-- Name: EXTENSION vector; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION vector IS 'vector data type and ivfflat and hnsw access methods';


--
-- Name: update_chat_long_term_memory_update_time(); Type: FUNCTION; Schema: public; Owner: fitness_user
--

CREATE FUNCTION public.update_chat_long_term_memory_update_time() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_chat_long_term_memory_update_time() OWNER TO fitness_user;

--
-- Name: update_fitness_video_course_update_time(); Type: FUNCTION; Schema: public; Owner: fitness_user
--

CREATE FUNCTION public.update_fitness_video_course_update_time() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_fitness_video_course_update_time() OWNER TO fitness_user;

--
-- Name: update_update_time_column(); Type: FUNCTION; Schema: public; Owner: fitness_user
--

CREATE FUNCTION public.update_update_time_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_update_time_column() OWNER TO fitness_user;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: analysis_report; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.analysis_report (
    id bigint NOT NULL,
    report_title character varying(255) NOT NULL,
    analysis_type character varying(50) NOT NULL,
    report_content text NOT NULL,
    suggestions text,
    generate_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    create_by bigint,
    is_deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.analysis_report OWNER TO fitness_user;

--
-- Name: analysis_report_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.analysis_report_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.analysis_report_id_seq OWNER TO fitness_user;

--
-- Name: analysis_report_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.analysis_report_id_seq OWNED BY public.analysis_report.id;


--
-- Name: chat_long_term_memory; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.chat_long_term_memory (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    memory_key character varying(100) NOT NULL,
    memory_type character varying(50) NOT NULL,
    content text NOT NULL,
    metadata jsonb DEFAULT '{}'::jsonb NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.chat_long_term_memory OWNER TO fitness_user;

--
-- Name: TABLE chat_long_term_memory; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.chat_long_term_memory IS '健小助长期记忆表';


--
-- Name: COLUMN chat_long_term_memory.user_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_long_term_memory.user_id IS '用户ID';


--
-- Name: COLUMN chat_long_term_memory.memory_key; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_long_term_memory.memory_key IS '记忆唯一键';


--
-- Name: COLUMN chat_long_term_memory.memory_type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_long_term_memory.memory_type IS '记忆类型';


--
-- Name: COLUMN chat_long_term_memory.content; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_long_term_memory.content IS '记忆内容';


--
-- Name: COLUMN chat_long_term_memory.metadata; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_long_term_memory.metadata IS '扩展元数据';


--
-- Name: chat_long_term_memory_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.chat_long_term_memory_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.chat_long_term_memory_id_seq OWNER TO fitness_user;

--
-- Name: chat_long_term_memory_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.chat_long_term_memory_id_seq OWNED BY public.chat_long_term_memory.id;


--
-- Name: chat_memory_store; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.chat_memory_store (
    id text NOT NULL,
    namespace text,
    key_name character varying(500),
    value_json text,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


ALTER TABLE public.chat_memory_store OWNER TO fitness_user;

--
-- Name: chat_message; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.chat_message (
    id bigint NOT NULL,
    session_id bigint NOT NULL,
    role character varying(20) NOT NULL,
    content text NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.chat_message OWNER TO fitness_user;

--
-- Name: TABLE chat_message; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.chat_message IS '智能客服对话消息表';


--
-- Name: COLUMN chat_message.id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_message.id IS '消息ID';


--
-- Name: COLUMN chat_message.session_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_message.session_id IS '会话ID';


--
-- Name: COLUMN chat_message.role; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_message.role IS '角色：user/assistant';


--
-- Name: COLUMN chat_message.content; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_message.content IS '消息内容';


--
-- Name: COLUMN chat_message.created_at; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_message.created_at IS '创建时间';


--
-- Name: chat_session; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.chat_session (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    title character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false
);


ALTER TABLE public.chat_session OWNER TO fitness_user;

--
-- Name: TABLE chat_session; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.chat_session IS '智能客服对话会话表';


--
-- Name: COLUMN chat_session.id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_session.id IS '会话ID';


--
-- Name: COLUMN chat_session.user_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_session.user_id IS '用户ID';


--
-- Name: COLUMN chat_session.title; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_session.title IS '会话标题';


--
-- Name: COLUMN chat_session.created_at; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_session.created_at IS '创建时间';


--
-- Name: COLUMN chat_session.updated_at; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_session.updated_at IS '更新时间';


--
-- Name: COLUMN chat_session.is_deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.chat_session.is_deleted IS '是否删除';


--
-- Name: coach_detail; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.coach_detail (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    personal_image_url character varying(500),
    tags jsonb DEFAULT '[]'::jsonb,
    work_years integer DEFAULT 0,
    specialties jsonb DEFAULT '[]'::jsonb,
    teaching_style character varying(50),
    education text,
    training text,
    languages jsonb DEFAULT '[]'::jsonb,
    bio text,
    experience text,
    honors jsonb DEFAULT '[]'::jsonb,
    emergency_contact jsonb DEFAULT '{}'::jsonb,
    certifications jsonb DEFAULT '[]'::jsonb,
    availability jsonb DEFAULT '{}'::jsonb,
    student_count integer DEFAULT 0,
    rating character varying(10) DEFAULT '99%'::character varying,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    real_name character varying(50) DEFAULT NULL::character varying,
    gender character varying(20) DEFAULT 'male'::character varying,
    age integer
);


ALTER TABLE public.coach_detail OWNER TO fitness_user;

--
-- Name: COLUMN coach_detail.real_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_detail.real_name IS '教练真实姓名，用于前端展示';


--
-- Name: coach_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.coach_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.coach_detail_id_seq OWNER TO fitness_user;

--
-- Name: coach_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.coach_detail_id_seq OWNED BY public.coach_detail.id;


--
-- Name: coach_notification; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.coach_notification (
    id bigint NOT NULL,
    coach_id bigint NOT NULL,
    student_id bigint NOT NULL,
    booking_id bigint,
    type character varying(50) DEFAULT 'BOOKING'::character varying NOT NULL,
    content character varying(500) NOT NULL,
    is_read boolean DEFAULT false NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.coach_notification OWNER TO fitness_user;

--
-- Name: TABLE coach_notification; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.coach_notification IS '教练通知表';


--
-- Name: COLUMN coach_notification.coach_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_notification.coach_id IS '教练用户ID';


--
-- Name: COLUMN coach_notification.student_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_notification.student_id IS '学员用户ID';


--
-- Name: COLUMN coach_notification.booking_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_notification.booking_id IS '关联预约ID';


--
-- Name: COLUMN coach_notification.type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_notification.type IS '通知类型：BOOKING-预约提醒';


--
-- Name: COLUMN coach_notification.content; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_notification.content IS '通知内容摘要';


--
-- Name: COLUMN coach_notification.is_read; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_notification.is_read IS '是否已读';


--
-- Name: coach_notification_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.coach_notification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.coach_notification_id_seq OWNER TO fitness_user;

--
-- Name: coach_notification_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.coach_notification_id_seq OWNED BY public.coach_notification.id;


--
-- Name: coach_package; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.coach_package (
    id bigint NOT NULL,
    coach_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    package_code character varying(50),
    description text,
    cover_image character varying(500),
    original_price numeric(10,2) DEFAULT 0 NOT NULL,
    total_sessions integer,
    validity_days integer,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    sort_order integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.coach_package OWNER TO fitness_user;

--
-- Name: TABLE coach_package; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.coach_package IS '私教套餐表';


--
-- Name: COLUMN coach_package.coach_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package.coach_id IS '所属教练用户ID';


--
-- Name: COLUMN coach_package.name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package.name IS '套餐名称';


--
-- Name: COLUMN coach_package.package_code; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package.package_code IS '套餐类型编码';


--
-- Name: COLUMN coach_package.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package.description IS '套餐描述';


--
-- Name: COLUMN coach_package.cover_image; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package.cover_image IS '封面图片URL';


--
-- Name: COLUMN coach_package.original_price; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package.original_price IS '套餐价格';


--
-- Name: COLUMN coach_package.total_sessions; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package.total_sessions IS '总课时数';


--
-- Name: COLUMN coach_package.validity_days; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package.validity_days IS '有效期天数';


--
-- Name: COLUMN coach_package.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package.status IS '状态：ACTIVE-上架，INACTIVE-下架';


--
-- Name: COLUMN coach_package.sort_order; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package.sort_order IS '排序号';


--
-- Name: coach_package_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.coach_package_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.coach_package_id_seq OWNER TO fitness_user;

--
-- Name: coach_package_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.coach_package_id_seq OWNED BY public.coach_package.id;


--
-- Name: coach_package_order_ext; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.coach_package_order_ext (
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    coach_package_id bigint,
    coach_id bigint NOT NULL,
    package_name character varying(100),
    expire_time timestamp without time zone
);


ALTER TABLE public.coach_package_order_ext OWNER TO fitness_user;

--
-- Name: TABLE coach_package_order_ext; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.coach_package_order_ext IS '私教套餐订单扩展表';


--
-- Name: COLUMN coach_package_order_ext.order_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package_order_ext.order_id IS '关联 orders.id';


--
-- Name: COLUMN coach_package_order_ext.coach_package_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package_order_ext.coach_package_id IS '私教套餐ID（关联 coach_package 表，V60 迁移后回填）';


--
-- Name: COLUMN coach_package_order_ext.coach_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package_order_ext.coach_id IS '教练ID';


--
-- Name: COLUMN coach_package_order_ext.package_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package_order_ext.package_name IS '套餐名称快照';


--
-- Name: COLUMN coach_package_order_ext.expire_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_package_order_ext.expire_time IS '订单过期时间（超时取消用）';


--
-- Name: coach_package_order_ext_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.coach_package_order_ext_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.coach_package_order_ext_id_seq OWNER TO fitness_user;

--
-- Name: coach_package_order_ext_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.coach_package_order_ext_id_seq OWNED BY public.coach_package_order_ext.id;


--
-- Name: coach_student; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.coach_student (
    id bigint NOT NULL,
    member_id bigint NOT NULL,
    coach_id bigint NOT NULL,
    product_id bigint,
    package_code character varying(50),
    bind_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expire_time timestamp without time zone,
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    coach_package_id bigint
);


ALTER TABLE public.coach_student OWNER TO fitness_user;

--
-- Name: TABLE coach_student; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.coach_student IS '教练-学员绑定表';


--
-- Name: COLUMN coach_student.member_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_student.member_id IS '学员用户ID（关联 sys_user）';


--
-- Name: COLUMN coach_student.coach_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_student.coach_id IS '教练用户ID（关联 sys_user）';


--
-- Name: COLUMN coach_student.product_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_student.product_id IS '购买的套餐商品ID（关联 product）';


--
-- Name: COLUMN coach_student.package_code; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_student.package_code IS '套餐编码（快照）';


--
-- Name: COLUMN coach_student.bind_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_student.bind_time IS '绑定时间（支付成功时间）';


--
-- Name: COLUMN coach_student.expire_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_student.expire_time IS '绑定到期时间';


--
-- Name: COLUMN coach_student.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.coach_student.status IS '状态：ACTIVE-有效，EXPIRED-已过期';


--
-- Name: coach_student_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.coach_student_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.coach_student_id_seq OWNER TO fitness_user;

--
-- Name: coach_student_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.coach_student_id_seq OWNED BY public.coach_student.id;


--
-- Name: fitness_booking; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.fitness_booking (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    course_id bigint NOT NULL,
    booking_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status smallint DEFAULT 0 NOT NULL,
    cancel_reason character varying(255),
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    session_id bigint
);


ALTER TABLE public.fitness_booking OWNER TO fitness_user;

--
-- Name: TABLE fitness_booking; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.fitness_booking IS '预约表';


--
-- Name: COLUMN fitness_booking.user_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_booking.user_id IS '用户ID';


--
-- Name: COLUMN fitness_booking.course_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_booking.course_id IS '课程ID';


--
-- Name: COLUMN fitness_booking.booking_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_booking.booking_time IS '预约时间';


--
-- Name: COLUMN fitness_booking.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_booking.status IS '状态: 0-待确认, 1-已确认, 2-已取消, 3-已完成';


--
-- Name: COLUMN fitness_booking.cancel_reason; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_booking.cancel_reason IS '取消原因';


--
-- Name: COLUMN fitness_booking.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_booking.deleted IS '软删除标志';


--
-- Name: COLUMN fitness_booking.session_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_booking.session_id IS '关联课程实例ID（周期性课程的某一次具体实例）';


--
-- Name: fitness_booking_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.fitness_booking_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitness_booking_id_seq OWNER TO fitness_user;

--
-- Name: fitness_booking_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.fitness_booking_id_seq OWNED BY public.fitness_booking.id;


--
-- Name: fitness_course; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.fitness_course (
    id bigint NOT NULL,
    course_name character varying(100) NOT NULL,
    description text,
    coach_id bigint NOT NULL,
    category character varying(50),
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    capacity integer DEFAULT 20 NOT NULL,
    status smallint DEFAULT 1 NOT NULL,
    image_url character varying(500),
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    difficulty_level character varying(20) DEFAULT '中级'::character varying,
    duration_minutes integer DEFAULT 45,
    calories_min integer DEFAULT 200,
    calories_max integer DEFAULT 500,
    total_booking_count integer DEFAULT 0,
    day_of_week smallint DEFAULT 1 NOT NULL
);


ALTER TABLE public.fitness_course OWNER TO fitness_user;

--
-- Name: TABLE fitness_course; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.fitness_course IS '课程表';


--
-- Name: COLUMN fitness_course.course_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.course_name IS '课程名称';


--
-- Name: COLUMN fitness_course.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.description IS '课程描述';


--
-- Name: COLUMN fitness_course.coach_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.coach_id IS '教练ID(关联sys_user)';


--
-- Name: COLUMN fitness_course.category; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.category IS '课程分类';


--
-- Name: COLUMN fitness_course.start_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.start_time IS '开始时间（时分秒，如 14:00:00）';


--
-- Name: COLUMN fitness_course.end_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.end_time IS '结束时间（时分秒，如 15:30:00）';


--
-- Name: COLUMN fitness_course.capacity; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.capacity IS '容量(最大人数)';


--
-- Name: COLUMN fitness_course.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.status IS '状态: 0-已取消, 1-可预约, 2-已满员, 3-已结束';


--
-- Name: COLUMN fitness_course.image_url; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.image_url IS '课程图片URL';


--
-- Name: COLUMN fitness_course.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.deleted IS '软删除标志';


--
-- Name: COLUMN fitness_course.difficulty_level; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.difficulty_level IS '难度等级：入门/初级/中级/高级/进阶';


--
-- Name: COLUMN fitness_course.duration_minutes; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.duration_minutes IS '课程时长（分钟）';


--
-- Name: COLUMN fitness_course.total_booking_count; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.total_booking_count IS '总预约人数（统计所有预约过该课程的独立会员数量）';


--
-- Name: COLUMN fitness_course.day_of_week; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course.day_of_week IS '星期几：1-周一, 2-周二, 3-周三, 4-周四, 5-周五, 6-周六, 7-周日';


--
-- Name: fitness_course_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.fitness_course_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitness_course_id_seq OWNER TO fitness_user;

--
-- Name: fitness_course_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.fitness_course_id_seq OWNED BY public.fitness_course.id;


--
-- Name: fitness_course_session; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.fitness_course_session (
    id bigint NOT NULL,
    course_id bigint NOT NULL,
    session_date date NOT NULL,
    day_of_week smallint NOT NULL,
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    status smallint DEFAULT 0 NOT NULL,
    capacity integer NOT NULL,
    booked_count integer DEFAULT 0 NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.fitness_course_session OWNER TO fitness_user;

--
-- Name: TABLE fitness_course_session; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.fitness_course_session IS '课程周实例表（周期性课程的具体某一次）';


--
-- Name: COLUMN fitness_course_session.course_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course_session.course_id IS '关联课程模板ID';


--
-- Name: COLUMN fitness_course_session.session_date; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course_session.session_date IS '本次上课的具体日期';


--
-- Name: COLUMN fitness_course_session.day_of_week; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course_session.day_of_week IS '星期几（冗余）';


--
-- Name: COLUMN fitness_course_session.start_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course_session.start_time IS '开始时间（时分秒，冗余）';


--
-- Name: COLUMN fitness_course_session.end_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course_session.end_time IS '结束时间（时分秒，冗余）';


--
-- Name: COLUMN fitness_course_session.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course_session.status IS '状态：0-待开始, 1-进行中, 2-已结束, 3-已取消';


--
-- Name: COLUMN fitness_course_session.capacity; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course_session.capacity IS '本次最大容量';


--
-- Name: COLUMN fitness_course_session.booked_count; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_course_session.booked_count IS '本次已预约数';


--
-- Name: fitness_course_session_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.fitness_course_session_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitness_course_session_id_seq OWNER TO fitness_user;

--
-- Name: fitness_course_session_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.fitness_course_session_id_seq OWNED BY public.fitness_course_session.id;


--
-- Name: fitness_equipment; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.fitness_equipment (
    id bigint NOT NULL,
    equipment_name character varying(100) NOT NULL,
    location character varying(200),
    status smallint DEFAULT 1 NOT NULL,
    description text,
    image_url character varying(500),
    purchase_date date,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    type_code character varying(50),
    equipment_no character varying(100)
);


ALTER TABLE public.fitness_equipment OWNER TO fitness_user;

--
-- Name: TABLE fitness_equipment; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.fitness_equipment IS '器材表';


--
-- Name: COLUMN fitness_equipment.equipment_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment.equipment_name IS '器材名称';


--
-- Name: COLUMN fitness_equipment.location; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment.location IS '存放位置';


--
-- Name: COLUMN fitness_equipment.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment.status IS '状态: 0-维修中, 1-正常, 2-报废';


--
-- Name: COLUMN fitness_equipment.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment.description IS '器材描述';


--
-- Name: COLUMN fitness_equipment.image_url; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment.image_url IS '器材图片URL';


--
-- Name: COLUMN fitness_equipment.purchase_date; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment.purchase_date IS '购买日期';


--
-- Name: COLUMN fitness_equipment.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment.deleted IS '软删除标志';


--
-- Name: fitness_equipment_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.fitness_equipment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitness_equipment_id_seq OWNER TO fitness_user;

--
-- Name: fitness_equipment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.fitness_equipment_id_seq OWNED BY public.fitness_equipment.id;


--
-- Name: fitness_equipment_repair; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.fitness_equipment_repair (
    id bigint NOT NULL,
    user_id bigint,
    description text NOT NULL,
    status smallint DEFAULT 0 NOT NULL,
    handle_time timestamp without time zone,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    image_urls text,
    handle_remark character varying(500),
    equipment_id bigint
);


ALTER TABLE public.fitness_equipment_repair OWNER TO fitness_user;

--
-- Name: TABLE fitness_equipment_repair; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.fitness_equipment_repair IS '器材报修表';


--
-- Name: COLUMN fitness_equipment_repair.user_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment_repair.user_id IS '报修人ID';


--
-- Name: COLUMN fitness_equipment_repair.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment_repair.description IS '问题描述';


--
-- Name: COLUMN fitness_equipment_repair.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment_repair.status IS '状态：0-待处理，1-处理中，2-已完成，3-已关闭';


--
-- Name: COLUMN fitness_equipment_repair.handle_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment_repair.handle_time IS '处理完成时间';


--
-- Name: COLUMN fitness_equipment_repair.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment_repair.deleted IS '软删除标志';


--
-- Name: COLUMN fitness_equipment_repair.image_urls; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment_repair.image_urls IS '报修图片URL列表，以逗号分隔';


--
-- Name: COLUMN fitness_equipment_repair.handle_remark; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_equipment_repair.handle_remark IS '处理备注';


--
-- Name: fitness_equipment_repair_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.fitness_equipment_repair_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitness_equipment_repair_id_seq OWNER TO fitness_user;

--
-- Name: fitness_equipment_repair_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.fitness_equipment_repair_id_seq OWNED BY public.fitness_equipment_repair.id;


--
-- Name: fitness_equipment_type; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.fitness_equipment_type (
    id bigint NOT NULL,
    type_code character varying(50) NOT NULL,
    type_name character varying(100) NOT NULL,
    icon character varying(255),
    sort_order integer DEFAULT 0,
    description text,
    deleted boolean DEFAULT false,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.fitness_equipment_type OWNER TO fitness_user;

--
-- Name: fitness_equipment_type_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.fitness_equipment_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitness_equipment_type_id_seq OWNER TO fitness_user;

--
-- Name: fitness_equipment_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.fitness_equipment_type_id_seq OWNED BY public.fitness_equipment_type.id;


--
-- Name: fitness_plan; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.fitness_plan (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    plan_name character varying(100) NOT NULL,
    goal character varying(255),
    duration integer,
    status smallint DEFAULT 1 NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    level character varying(50),
    height numeric(5,2) DEFAULT NULL::numeric,
    weight numeric(5,2) DEFAULT NULL::numeric,
    age integer,
    gender character varying(10) DEFAULT NULL::character varying,
    experience character varying(20) DEFAULT NULL::character varying,
    fitness_goal character varying(50) DEFAULT NULL::character varying,
    plan_data_json jsonb
);


ALTER TABLE public.fitness_plan OWNER TO fitness_user;

--
-- Name: TABLE fitness_plan; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.fitness_plan IS '健身计划表';


--
-- Name: COLUMN fitness_plan.user_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.user_id IS '用户ID';


--
-- Name: COLUMN fitness_plan.plan_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.plan_name IS '计划名称';


--
-- Name: COLUMN fitness_plan.goal; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.goal IS '健身目标';


--
-- Name: COLUMN fitness_plan.duration; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.duration IS '计划周期(天)';


--
-- Name: COLUMN fitness_plan.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.status IS '状态: 0-已停用, 1-进行中';


--
-- Name: COLUMN fitness_plan.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.deleted IS '软删除标志';


--
-- Name: COLUMN fitness_plan.level; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.level IS '难度等级: BEGINNER-初级, INTERMEDIATE-中级, ADVANCED-高级';


--
-- Name: COLUMN fitness_plan.height; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.height IS '身高(cm)';


--
-- Name: COLUMN fitness_plan.weight; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.weight IS '体重(kg)';


--
-- Name: COLUMN fitness_plan.age; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.age IS '年龄';


--
-- Name: COLUMN fitness_plan.gender; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.gender IS '性别';


--
-- Name: COLUMN fitness_plan.experience; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.experience IS '健身经验';


--
-- Name: COLUMN fitness_plan.fitness_goal; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.fitness_goal IS '健身目标';


--
-- Name: COLUMN fitness_plan.plan_data_json; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan.plan_data_json IS 'LLM返回的完整健身计划JSON数据';


--
-- Name: fitness_plan_detail; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.fitness_plan_detail (
    id bigint NOT NULL,
    plan_id bigint NOT NULL,
    day_of_week smallint NOT NULL,
    exercise_name character varying(100) NOT NULL,
    sets integer,
    reps integer,
    duration integer,
    notes character varying(500),
    sort_order integer DEFAULT 0,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    course_id bigint,
    equipment_names text,
    day_index smallint,
    day_name character varying(20),
    focus character varying(100),
    course_name character varying(200),
    equipment_json text,
    exercises_json text
);


ALTER TABLE public.fitness_plan_detail OWNER TO fitness_user;

--
-- Name: TABLE fitness_plan_detail; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.fitness_plan_detail IS '健身计划详情表';


--
-- Name: COLUMN fitness_plan_detail.plan_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.plan_id IS '计划ID';


--
-- Name: COLUMN fitness_plan_detail.day_of_week; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.day_of_week IS '星期几(1-7)';


--
-- Name: COLUMN fitness_plan_detail.exercise_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.exercise_name IS '运动名称';


--
-- Name: COLUMN fitness_plan_detail.sets; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.sets IS '组数';


--
-- Name: COLUMN fitness_plan_detail.reps; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.reps IS '每组次数';


--
-- Name: COLUMN fitness_plan_detail.duration; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.duration IS '持续时间(分钟)';


--
-- Name: COLUMN fitness_plan_detail.notes; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.notes IS '备注';


--
-- Name: COLUMN fitness_plan_detail.sort_order; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.sort_order IS '排序';


--
-- Name: COLUMN fitness_plan_detail.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.deleted IS '软删除标志';


--
-- Name: COLUMN fitness_plan_detail.course_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.course_id IS '课程ID';


--
-- Name: COLUMN fitness_plan_detail.equipment_names; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.equipment_names IS '推荐器械名称(JSON数组格式)';


--
-- Name: COLUMN fitness_plan_detail.day_index; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.day_index IS '天数索引(1-7)';


--
-- Name: COLUMN fitness_plan_detail.day_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.day_name IS '星期名称(周一、周二等)';


--
-- Name: COLUMN fitness_plan_detail.focus; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.focus IS '训练重点(胸部、背部等)';


--
-- Name: COLUMN fitness_plan_detail.course_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.course_name IS '课程名称';


--
-- Name: COLUMN fitness_plan_detail.equipment_json; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.equipment_json IS '器械信息(JSON格式)';


--
-- Name: COLUMN fitness_plan_detail.exercises_json; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_plan_detail.exercises_json IS '训练动作(JSON格式)';


--
-- Name: fitness_plan_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.fitness_plan_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitness_plan_detail_id_seq OWNER TO fitness_user;

--
-- Name: fitness_plan_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.fitness_plan_detail_id_seq OWNED BY public.fitness_plan_detail.id;


--
-- Name: fitness_plan_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.fitness_plan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitness_plan_id_seq OWNER TO fitness_user;

--
-- Name: fitness_plan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.fitness_plan_id_seq OWNED BY public.fitness_plan.id;


--
-- Name: fitness_private_coach_booking; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.fitness_private_coach_booking (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    coach_id bigint NOT NULL,
    booking_date date NOT NULL,
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    note character varying(500),
    status smallint DEFAULT 0 NOT NULL,
    cancel_reason character varying(255),
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.fitness_private_coach_booking OWNER TO fitness_user;

--
-- Name: TABLE fitness_private_coach_booking; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.fitness_private_coach_booking IS '私教课程预约表';


--
-- Name: COLUMN fitness_private_coach_booking.user_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_private_coach_booking.user_id IS '会员用户ID';


--
-- Name: COLUMN fitness_private_coach_booking.coach_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_private_coach_booking.coach_id IS '教练用户ID';


--
-- Name: COLUMN fitness_private_coach_booking.booking_date; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_private_coach_booking.booking_date IS '预约上课日期';


--
-- Name: COLUMN fitness_private_coach_booking.start_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_private_coach_booking.start_time IS '上课开始时间';


--
-- Name: COLUMN fitness_private_coach_booking.end_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_private_coach_booking.end_time IS '上课结束时间';


--
-- Name: COLUMN fitness_private_coach_booking.note; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_private_coach_booking.note IS '会员备注';


--
-- Name: COLUMN fitness_private_coach_booking.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_private_coach_booking.status IS '状态：0-待确认，1-已确认，2-已取消，3-已完成';


--
-- Name: COLUMN fitness_private_coach_booking.cancel_reason; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_private_coach_booking.cancel_reason IS '取消原因';


--
-- Name: COLUMN fitness_private_coach_booking.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_private_coach_booking.deleted IS '软删除标识';


--
-- Name: fitness_private_coach_booking_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.fitness_private_coach_booking_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitness_private_coach_booking_id_seq OWNER TO fitness_user;

--
-- Name: fitness_private_coach_booking_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.fitness_private_coach_booking_id_seq OWNED BY public.fitness_private_coach_booking.id;


--
-- Name: fitness_repair_record; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.fitness_repair_record (
    id bigint NOT NULL,
    repair_id bigint NOT NULL,
    handler_id bigint,
    record_type smallint DEFAULT 1 NOT NULL,
    before_status smallint,
    after_status smallint,
    content text,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.fitness_repair_record OWNER TO fitness_user;

--
-- Name: TABLE fitness_repair_record; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.fitness_repair_record IS '报修处理记录表';


--
-- Name: COLUMN fitness_repair_record.repair_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_repair_record.repair_id IS '报修ID';


--
-- Name: COLUMN fitness_repair_record.handler_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_repair_record.handler_id IS '处理人ID（管理员）';


--
-- Name: COLUMN fitness_repair_record.record_type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_repair_record.record_type IS '处理类型：1-提交报修, 2-状态变更, 3-处理备注, 4-取消报修';


--
-- Name: COLUMN fitness_repair_record.before_status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_repair_record.before_status IS '处理前状态';


--
-- Name: COLUMN fitness_repair_record.after_status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_repair_record.after_status IS '处理后状态';


--
-- Name: COLUMN fitness_repair_record.content; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_repair_record.content IS '处理内容/备注';


--
-- Name: COLUMN fitness_repair_record.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_repair_record.deleted IS '软删除标识';


--
-- Name: fitness_repair_record_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.fitness_repair_record_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitness_repair_record_id_seq OWNER TO fitness_user;

--
-- Name: fitness_repair_record_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.fitness_repair_record_id_seq OWNED BY public.fitness_repair_record.id;


--
-- Name: fitness_video_course; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.fitness_video_course (
    id bigint NOT NULL,
    title character varying(200) NOT NULL,
    description text,
    category character varying(100),
    cover_url character varying(500),
    video_url character varying(500) NOT NULL,
    duration_seconds integer DEFAULT 0,
    file_size bigint DEFAULT 0,
    difficulty_level character varying(20),
    coach_id bigint,
    status smallint DEFAULT 1 NOT NULL,
    view_count integer DEFAULT 0,
    sort_order integer DEFAULT 0,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.fitness_video_course OWNER TO fitness_user;

--
-- Name: TABLE fitness_video_course; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.fitness_video_course IS '视频课程表';


--
-- Name: COLUMN fitness_video_course.title; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.title IS '视频标题';


--
-- Name: COLUMN fitness_video_course.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.description IS '视频描述';


--
-- Name: COLUMN fitness_video_course.category; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.category IS '分类';


--
-- Name: COLUMN fitness_video_course.cover_url; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.cover_url IS '封面图片URL';


--
-- Name: COLUMN fitness_video_course.video_url; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.video_url IS '视频文件URL';


--
-- Name: COLUMN fitness_video_course.duration_seconds; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.duration_seconds IS '视频时长(秒)';


--
-- Name: COLUMN fitness_video_course.file_size; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.file_size IS '文件大小(字节)';


--
-- Name: COLUMN fitness_video_course.difficulty_level; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.difficulty_level IS '难度等级';


--
-- Name: COLUMN fitness_video_course.coach_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.coach_id IS '教练ID';


--
-- Name: COLUMN fitness_video_course.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.status IS '状态：0-下架，1-上架';


--
-- Name: COLUMN fitness_video_course.view_count; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.view_count IS '观看次数';


--
-- Name: COLUMN fitness_video_course.sort_order; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.sort_order IS '排序';


--
-- Name: COLUMN fitness_video_course.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.fitness_video_course.deleted IS '软删除标识';


--
-- Name: fitness_video_course_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.fitness_video_course_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fitness_video_course_id_seq OWNER TO fitness_user;

--
-- Name: fitness_video_course_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.fitness_video_course_id_seq OWNED BY public.fitness_video_course.id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO fitness_user;

--
-- Name: knowledge_category; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.knowledge_category (
    id bigint NOT NULL,
    name character varying(50) NOT NULL,
    code character varying(50) NOT NULL,
    description character varying(255),
    sort_order integer DEFAULT 0,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.knowledge_category OWNER TO fitness_user;

--
-- Name: TABLE knowledge_category; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.knowledge_category IS '知识库分类表';


--
-- Name: COLUMN knowledge_category.name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_category.name IS '分类名称';


--
-- Name: COLUMN knowledge_category.code; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_category.code IS '分类编码';


--
-- Name: COLUMN knowledge_category.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_category.description IS '分类描述';


--
-- Name: COLUMN knowledge_category.sort_order; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_category.sort_order IS '排序';


--
-- Name: COLUMN knowledge_category.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_category.deleted IS '软删除标识';


--
-- Name: knowledge_category_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.knowledge_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.knowledge_category_id_seq OWNER TO fitness_user;

--
-- Name: knowledge_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.knowledge_category_id_seq OWNED BY public.knowledge_category.id;


--
-- Name: knowledge_chunk; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.knowledge_chunk (
    id bigint NOT NULL,
    document_id bigint NOT NULL,
    chunk_index integer NOT NULL,
    content text NOT NULL,
    content_hash character varying(64),
    embedding public.vector(768),
    metadata jsonb DEFAULT '{}'::jsonb,
    char_count integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.knowledge_chunk OWNER TO fitness_user;

--
-- Name: TABLE knowledge_chunk; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.knowledge_chunk IS '知识库切片表';


--
-- Name: COLUMN knowledge_chunk.document_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_chunk.document_id IS '关联文档ID';


--
-- Name: COLUMN knowledge_chunk.chunk_index; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_chunk.chunk_index IS '切片序号(从0开始)';


--
-- Name: COLUMN knowledge_chunk.content; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_chunk.content IS '切片文本内容';


--
-- Name: COLUMN knowledge_chunk.content_hash; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_chunk.content_hash IS '内容哈希(MD5)';


--
-- Name: COLUMN knowledge_chunk.embedding; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_chunk.embedding IS '向量嵌入(768维)';


--
-- Name: COLUMN knowledge_chunk.metadata; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_chunk.metadata IS '元数据(JSONB)';


--
-- Name: COLUMN knowledge_chunk.char_count; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_chunk.char_count IS '字符数';


--
-- Name: knowledge_chunk_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.knowledge_chunk_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.knowledge_chunk_id_seq OWNER TO fitness_user;

--
-- Name: knowledge_chunk_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.knowledge_chunk_id_seq OWNED BY public.knowledge_chunk.id;


--
-- Name: knowledge_document; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.knowledge_document (
    id bigint NOT NULL,
    title character varying(200) NOT NULL,
    file_url character varying(500),
    file_name character varying(255),
    file_type character varying(50),
    file_size bigint DEFAULT 0,
    status smallint DEFAULT 0 NOT NULL,
    chunk_count integer DEFAULT 0,
    create_by bigint,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    category_id bigint
);


ALTER TABLE public.knowledge_document OWNER TO fitness_user;

--
-- Name: TABLE knowledge_document; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.knowledge_document IS '知识库文档表';


--
-- Name: COLUMN knowledge_document.title; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_document.title IS '文档标题';


--
-- Name: COLUMN knowledge_document.file_url; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_document.file_url IS 'MinIO文件URL';


--
-- Name: COLUMN knowledge_document.file_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_document.file_name IS '原始文件名';


--
-- Name: COLUMN knowledge_document.file_type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_document.file_type IS '文件类型(md/txt)';


--
-- Name: COLUMN knowledge_document.file_size; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_document.file_size IS '文件大小(字节)';


--
-- Name: COLUMN knowledge_document.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_document.status IS '状态：0-草稿，1-已发布，2-已归档';


--
-- Name: COLUMN knowledge_document.chunk_count; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_document.chunk_count IS '切片数量';


--
-- Name: COLUMN knowledge_document.create_by; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_document.create_by IS '创建人ID';


--
-- Name: COLUMN knowledge_document.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_document.deleted IS '软删除标识';


--
-- Name: COLUMN knowledge_document.category_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.knowledge_document.category_id IS '分类ID，关联 knowledge_category 表';


--
-- Name: knowledge_document_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.knowledge_document_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.knowledge_document_id_seq OWNER TO fitness_user;

--
-- Name: knowledge_document_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.knowledge_document_id_seq OWNED BY public.knowledge_document.id;


--
-- Name: membership_card; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.membership_card (
    id bigint NOT NULL,
    type_id bigint NOT NULL,
    name character varying(200) NOT NULL,
    subtitle character varying(500),
    price numeric(10,2) NOT NULL,
    original_price numeric(10,2),
    duration_days integer NOT NULL,
    points_reward integer DEFAULT 0,
    daily_price numeric(10,2),
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    is_recommend boolean DEFAULT false,
    sort_order integer DEFAULT 0,
    cover_image character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    type_code character varying(50)
);


ALTER TABLE public.membership_card OWNER TO fitness_user;

--
-- Name: TABLE membership_card; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.membership_card IS '会员卡定义表';


--
-- Name: COLUMN membership_card.type_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.type_id IS '会员卡类型ID';


--
-- Name: COLUMN membership_card.name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.name IS '会员卡名称';


--
-- Name: COLUMN membership_card.subtitle; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.subtitle IS '副标题/简介';


--
-- Name: COLUMN membership_card.price; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.price IS '售价';


--
-- Name: COLUMN membership_card.original_price; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.original_price IS '原价';


--
-- Name: COLUMN membership_card.duration_days; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.duration_days IS '有效期天数';


--
-- Name: COLUMN membership_card.points_reward; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.points_reward IS '购买赠送积分';


--
-- Name: COLUMN membership_card.daily_price; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.daily_price IS '日均价格（用于展示）';


--
-- Name: COLUMN membership_card.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.status IS '状态：ACTIVE-上架，INACTIVE-下架';


--
-- Name: COLUMN membership_card.is_recommend; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.is_recommend IS '是否推荐';


--
-- Name: COLUMN membership_card.sort_order; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.sort_order IS '排序号';


--
-- Name: COLUMN membership_card.cover_image; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.cover_image IS '封面图片URL';


--
-- Name: COLUMN membership_card.type_code; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card.type_code IS '会员卡类型编码（冗余字段，便于查询）';


--
-- Name: membership_card_content; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.membership_card_content (
    id bigint NOT NULL,
    card_id bigint NOT NULL,
    content_type character varying(50) NOT NULL,
    title character varying(200) NOT NULL,
    description text,
    icon character varying(200),
    sort_order integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.membership_card_content OWNER TO fitness_user;

--
-- Name: TABLE membership_card_content; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.membership_card_content IS '会员卡内容项表';


--
-- Name: COLUMN membership_card_content.card_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card_content.card_id IS '会员卡ID';


--
-- Name: COLUMN membership_card_content.content_type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card_content.content_type IS '内容类型：BENEFIT-权益说明, RULE-使用规则, PRIVILEGE-特权列表, OTHER-其他';


--
-- Name: COLUMN membership_card_content.title; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card_content.title IS '标题';


--
-- Name: COLUMN membership_card_content.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card_content.description IS '详细描述';


--
-- Name: COLUMN membership_card_content.icon; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card_content.icon IS '图标';


--
-- Name: COLUMN membership_card_content.sort_order; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card_content.sort_order IS '排序号';


--
-- Name: membership_card_content_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.membership_card_content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.membership_card_content_id_seq OWNER TO fitness_user;

--
-- Name: membership_card_content_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.membership_card_content_id_seq OWNED BY public.membership_card_content.id;


--
-- Name: membership_card_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.membership_card_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.membership_card_id_seq OWNER TO fitness_user;

--
-- Name: membership_card_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.membership_card_id_seq OWNED BY public.membership_card.id;


--
-- Name: membership_card_type; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.membership_card_type (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    code character varying(50) NOT NULL,
    description text,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    sort_order integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.membership_card_type OWNER TO fitness_user;

--
-- Name: TABLE membership_card_type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.membership_card_type IS '会员卡类型表';


--
-- Name: COLUMN membership_card_type.name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card_type.name IS '类型名称，如：月卡、季卡、年卡';


--
-- Name: COLUMN membership_card_type.code; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card_type.code IS '类型编码';


--
-- Name: COLUMN membership_card_type.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card_type.description IS '类型描述';


--
-- Name: COLUMN membership_card_type.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card_type.status IS '状态：ACTIVE-启用，INACTIVE-禁用';


--
-- Name: COLUMN membership_card_type.sort_order; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_card_type.sort_order IS '排序号';


--
-- Name: membership_card_type_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.membership_card_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.membership_card_type_id_seq OWNER TO fitness_user;

--
-- Name: membership_card_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.membership_card_type_id_seq OWNED BY public.membership_card_type.id;


--
-- Name: membership_order_ext; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.membership_order_ext (
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    card_id bigint NOT NULL,
    card_name character varying(200),
    expire_time timestamp without time zone
);


ALTER TABLE public.membership_order_ext OWNER TO fitness_user;

--
-- Name: TABLE membership_order_ext; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.membership_order_ext IS '会员卡订单扩展表';


--
-- Name: COLUMN membership_order_ext.order_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_ext.order_id IS '关联 orders.id';


--
-- Name: COLUMN membership_order_ext.card_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_ext.card_id IS '会员卡ID';


--
-- Name: COLUMN membership_order_ext.card_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_ext.card_name IS '会员卡名称快照';


--
-- Name: COLUMN membership_order_ext.expire_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_ext.expire_time IS '会员到期时间';


--
-- Name: membership_order_ext_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.membership_order_ext_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.membership_order_ext_id_seq OWNER TO fitness_user;

--
-- Name: membership_order_ext_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.membership_order_ext_id_seq OWNED BY public.membership_order_ext.id;


--
-- Name: membership_order_old; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.membership_order_old (
    id bigint NOT NULL,
    order_no character varying(64) NOT NULL,
    user_id bigint NOT NULL,
    card_id bigint NOT NULL,
    card_name character varying(200),
    price numeric(10,2) NOT NULL,
    pay_amount numeric(10,2),
    pay_method character varying(50),
    pay_time timestamp without time zone,
    status character varying(20) DEFAULT 'PENDING'::character varying,
    expire_time timestamp without time zone,
    alipay_trade_no character varying(128),
    remark character varying(500),
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.membership_order_old OWNER TO fitness_user;

--
-- Name: TABLE membership_order_old; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.membership_order_old IS '会员卡订单表';


--
-- Name: COLUMN membership_order_old.order_no; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.order_no IS '订单编号';


--
-- Name: COLUMN membership_order_old.user_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.user_id IS '用户ID';


--
-- Name: COLUMN membership_order_old.card_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.card_id IS '会员卡ID';


--
-- Name: COLUMN membership_order_old.card_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.card_name IS '会员卡名称（快照）';


--
-- Name: COLUMN membership_order_old.price; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.price IS '订单金额';


--
-- Name: COLUMN membership_order_old.pay_amount; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.pay_amount IS '实际支付金额';


--
-- Name: COLUMN membership_order_old.pay_method; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.pay_method IS '支付方式：ALIPAY-支付宝, BALANCE-余额';


--
-- Name: COLUMN membership_order_old.pay_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.pay_time IS '支付时间';


--
-- Name: COLUMN membership_order_old.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.status IS '状态：PENDING-待支付, PAID-已支付, CANCELLED-已取消, TIMEOUT-已超时';


--
-- Name: COLUMN membership_order_old.expire_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.expire_time IS '会员到期时间';


--
-- Name: COLUMN membership_order_old.alipay_trade_no; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.alipay_trade_no IS '支付宝交易号';


--
-- Name: COLUMN membership_order_old.remark; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.remark IS '备注';


--
-- Name: COLUMN membership_order_old.create_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.create_time IS '创建时间';


--
-- Name: COLUMN membership_order_old.update_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.update_time IS '更新时间';


--
-- Name: COLUMN membership_order_old.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.membership_order_old.deleted IS '逻辑删除标志';


--
-- Name: membership_order_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.membership_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.membership_order_id_seq OWNER TO fitness_user;

--
-- Name: membership_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.membership_order_id_seq OWNED BY public.membership_order_old.id;


--
-- Name: orders; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.orders (
    id bigint NOT NULL,
    order_no character varying(64) NOT NULL,
    user_id bigint NOT NULL,
    order_type character varying(30) NOT NULL,
    original_amount numeric(10,2) NOT NULL,
    discount_amount numeric(10,2) DEFAULT 0,
    pay_amount numeric(10,2) NOT NULL,
    pay_method character varying(50),
    pay_time timestamp without time zone,
    alipay_trade_no character varying(128),
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    remark character varying(500),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.orders OWNER TO fitness_user;

--
-- Name: TABLE orders; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.orders IS '统一订单基表';


--
-- Name: COLUMN orders.order_no; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.orders.order_no IS '统一订单编号';


--
-- Name: COLUMN orders.user_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.orders.user_id IS '用户ID';


--
-- Name: COLUMN orders.order_type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.orders.order_type IS '订单类型：PRODUCT-商品，COACH_PACKAGE-私教套餐，MEMBERSHIP-会员卡';


--
-- Name: COLUMN orders.original_amount; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.orders.original_amount IS '原始金额';


--
-- Name: COLUMN orders.discount_amount; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.orders.discount_amount IS '优惠金额';


--
-- Name: COLUMN orders.pay_amount; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.orders.pay_amount IS '实付金额';


--
-- Name: COLUMN orders.pay_method; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.orders.pay_method IS '支付方式：ALIPAY-支付宝，BALANCE-余额';


--
-- Name: COLUMN orders.pay_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.orders.pay_time IS '支付时间';


--
-- Name: COLUMN orders.alipay_trade_no; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.orders.alipay_trade_no IS '支付宝交易号';


--
-- Name: COLUMN orders.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.orders.status IS '状态：PENDING-待支付，PAID-已支付，COMPLETED-已完成，CANCELLED-已取消，TIMEOUT-已超时';


--
-- Name: COLUMN orders.remark; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.orders.remark IS '备注';


--
-- Name: orders_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.orders_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.orders_id_seq OWNER TO fitness_user;

--
-- Name: orders_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.orders_id_seq OWNED BY public.orders.id;


--
-- Name: product; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.product (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    code character varying(50),
    category character varying(50) NOT NULL,
    image_url character varying(500),
    description text,
    original_price numeric(10,2) DEFAULT 0 NOT NULL,
    points_discount_type character varying(20) DEFAULT 'FIXED'::character varying,
    points_discount_value numeric(10,2) DEFAULT 0,
    max_points_discount numeric(10,2) DEFAULT 0,
    stock integer DEFAULT 0 NOT NULL,
    sales integer DEFAULT 0,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    is_hot boolean DEFAULT false,
    is_new boolean DEFAULT false,
    is_recommend boolean DEFAULT false,
    sort_order integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.product OWNER TO fitness_user;

--
-- Name: TABLE product; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.product IS '商品表';


--
-- Name: COLUMN product.name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.name IS '商品名称';


--
-- Name: COLUMN product.code; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.code IS '商品编号';


--
-- Name: COLUMN product.category; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.category IS '商品分类：EQUIPMENT-运动装备，SUPPLEMENT-营养补剂，COURSE-课程优惠，OTHER-其他';


--
-- Name: COLUMN product.image_url; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.image_url IS '商品图片 URL';


--
-- Name: COLUMN product.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.description IS '商品描述';


--
-- Name: COLUMN product.original_price; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.original_price IS '商品原价';


--
-- Name: COLUMN product.points_discount_type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.points_discount_type IS '积分抵扣类型：FIXED-固定金额，PERCENT-比例，NONE-不支持';


--
-- Name: COLUMN product.points_discount_value; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.points_discount_value IS '积分抵扣值 (固定金额或比例)';


--
-- Name: COLUMN product.max_points_discount; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.max_points_discount IS '最大积分抵扣金额';


--
-- Name: COLUMN product.stock; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.stock IS '库存数量';


--
-- Name: COLUMN product.sales; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.sales IS '销量';


--
-- Name: COLUMN product.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.status IS '状态：ACTIVE-上架，INACTIVE-下架';


--
-- Name: COLUMN product.is_hot; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.is_hot IS '是否热销';


--
-- Name: COLUMN product.is_new; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.is_new IS '是否新品';


--
-- Name: COLUMN product.is_recommend; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.is_recommend IS '是否推荐';


--
-- Name: COLUMN product.sort_order; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product.sort_order IS '排序';


--
-- Name: product_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.product_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.product_id_seq OWNER TO fitness_user;

--
-- Name: product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.product_id_seq OWNED BY public.product.id;


--
-- Name: product_order_ext; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.product_order_ext (
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    product_id bigint NOT NULL,
    product_name character varying(100),
    quantity integer DEFAULT 1,
    points_used integer DEFAULT 0,
    points_discount numeric(10,2) DEFAULT 0,
    tracking_no character varying(100),
    carrier character varying(50),
    address text,
    pickup_type character varying(20) DEFAULT 'IN_STORE'::character varying,
    pickup_code character varying(20),
    pickup_status character varying(20) DEFAULT 'NOT_PICKED'::character varying,
    pickup_time timestamp without time zone
);


ALTER TABLE public.product_order_ext OWNER TO fitness_user;

--
-- Name: TABLE product_order_ext; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.product_order_ext IS '商品订单扩展表';


--
-- Name: COLUMN product_order_ext.order_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.order_id IS '关联 orders.id';


--
-- Name: COLUMN product_order_ext.product_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.product_id IS '商品ID';


--
-- Name: COLUMN product_order_ext.product_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.product_name IS '商品名称快照';


--
-- Name: COLUMN product_order_ext.quantity; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.quantity IS '购买数量';


--
-- Name: COLUMN product_order_ext.points_used; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.points_used IS '使用积分数量';


--
-- Name: COLUMN product_order_ext.points_discount; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.points_discount IS '积分抵扣金额';


--
-- Name: COLUMN product_order_ext.tracking_no; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.tracking_no IS '物流单号';


--
-- Name: COLUMN product_order_ext.carrier; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.carrier IS '物流公司';


--
-- Name: COLUMN product_order_ext.address; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.address IS '收货地址';


--
-- Name: COLUMN product_order_ext.pickup_type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.pickup_type IS '取货方式：IN_STORE-到店自取，DELIVERY-物流配送';


--
-- Name: COLUMN product_order_ext.pickup_code; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.pickup_code IS '取货验证码';


--
-- Name: COLUMN product_order_ext.pickup_status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.pickup_status IS '取货状态：NOT_PICKED-未取货，PICKED-已取货';


--
-- Name: COLUMN product_order_ext.pickup_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_ext.pickup_time IS '取货时间';


--
-- Name: product_order_ext_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.product_order_ext_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.product_order_ext_id_seq OWNER TO fitness_user;

--
-- Name: product_order_ext_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.product_order_ext_id_seq OWNED BY public.product_order_ext.id;


--
-- Name: product_order_old; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.product_order_old (
    id bigint NOT NULL,
    order_no character varying(50) NOT NULL,
    user_id bigint NOT NULL,
    product_id bigint NOT NULL,
    product_name character varying(100) NOT NULL,
    quantity integer DEFAULT 1 NOT NULL,
    original_price numeric(10,2) NOT NULL,
    points_used integer DEFAULT 0,
    points_discount numeric(10,2) DEFAULT 0,
    final_price numeric(10,2) NOT NULL,
    pay_amount numeric(10,2) NOT NULL,
    pay_method character varying(50),
    pay_time timestamp without time zone,
    status character varying(20) DEFAULT 'PENDING'::character varying,
    tracking_no character varying(100),
    carrier character varying(50),
    address text,
    remark text,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    pickup_code character varying(20),
    pickup_status character varying(20) DEFAULT 'NOT_PICKED'::character varying,
    pickup_time timestamp without time zone,
    expire_time timestamp without time zone,
    alipay_trade_no character varying(100),
    pickup_type character varying(20) DEFAULT 'IN_STORE'::character varying
);


ALTER TABLE public.product_order_old OWNER TO fitness_user;

--
-- Name: TABLE product_order_old; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.product_order_old IS '商品订单表';


--
-- Name: COLUMN product_order_old.order_no; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.order_no IS '订单编号';


--
-- Name: COLUMN product_order_old.user_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.user_id IS '用户 ID';


--
-- Name: COLUMN product_order_old.product_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.product_id IS '商品 ID';


--
-- Name: COLUMN product_order_old.product_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.product_name IS '商品名称';


--
-- Name: COLUMN product_order_old.quantity; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.quantity IS '购买数量';


--
-- Name: COLUMN product_order_old.original_price; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.original_price IS '商品原价';


--
-- Name: COLUMN product_order_old.points_used; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.points_used IS '使用积分数量';


--
-- Name: COLUMN product_order_old.points_discount; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.points_discount IS '积分抵扣金额';


--
-- Name: COLUMN product_order_old.final_price; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.final_price IS '最终支付价格';


--
-- Name: COLUMN product_order_old.pay_amount; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.pay_amount IS '实际支付金额';


--
-- Name: COLUMN product_order_old.pay_method; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.pay_method IS '支付方式';


--
-- Name: COLUMN product_order_old.pay_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.pay_time IS '支付时间';


--
-- Name: COLUMN product_order_old.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.status IS '订单状态：PENDING-待支付，PAID-已支付，PROCESSING-处理中，SHIPPED-已发货，COMPLETED-已完成，CANCELLED-已取消';


--
-- Name: COLUMN product_order_old.tracking_no; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.tracking_no IS '物流单号';


--
-- Name: COLUMN product_order_old.carrier; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.carrier IS '物流公司';


--
-- Name: COLUMN product_order_old.address; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.address IS '收货地址';


--
-- Name: COLUMN product_order_old.remark; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.remark IS '备注';


--
-- Name: COLUMN product_order_old.pickup_code; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.pickup_code IS '取货验证码（6位）';


--
-- Name: COLUMN product_order_old.pickup_status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.pickup_status IS '取货状态：NOT_PICKED-未取货，PICKED-已取货';


--
-- Name: COLUMN product_order_old.pickup_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.pickup_time IS '取货时间';


--
-- Name: COLUMN product_order_old.expire_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.expire_time IS '订单过期时间';


--
-- Name: COLUMN product_order_old.alipay_trade_no; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.alipay_trade_no IS '支付宝交易号';


--
-- Name: COLUMN product_order_old.pickup_type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.product_order_old.pickup_type IS '取货方式：IN_STORE-到店自取，DELIVERY-物流配送';


--
-- Name: product_order_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.product_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.product_order_id_seq OWNER TO fitness_user;

--
-- Name: product_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.product_order_id_seq OWNED BY public.product_order_old.id;


--
-- Name: sys_announcement; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.sys_announcement (
    id bigint NOT NULL,
    title character varying(200) NOT NULL,
    content text NOT NULL,
    type character varying(50) DEFAULT 'SYSTEM'::character varying NOT NULL,
    status smallint DEFAULT 0 NOT NULL,
    view_count integer DEFAULT 0 NOT NULL,
    publish_time timestamp without time zone,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.sys_announcement OWNER TO fitness_user;

--
-- Name: TABLE sys_announcement; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.sys_announcement IS '公告表';


--
-- Name: COLUMN sys_announcement.title; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_announcement.title IS '公告标题';


--
-- Name: COLUMN sys_announcement.content; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_announcement.content IS '公告内容';


--
-- Name: COLUMN sys_announcement.type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_announcement.type IS '公告类型：SYSTEM-系统公告, ACTIVITY-活动通知, IMPORTANT-重要提醒';


--
-- Name: COLUMN sys_announcement.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_announcement.status IS '状态：0-草稿，1-已发布';


--
-- Name: COLUMN sys_announcement.view_count; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_announcement.view_count IS '浏览量';


--
-- Name: COLUMN sys_announcement.publish_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_announcement.publish_time IS '发布时间';


--
-- Name: COLUMN sys_announcement.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_announcement.deleted IS '软删除标识';


--
-- Name: sys_announcement_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.sys_announcement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_announcement_id_seq OWNER TO fitness_user;

--
-- Name: sys_announcement_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.sys_announcement_id_seq OWNED BY public.sys_announcement.id;


--
-- Name: sys_banner; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.sys_banner (
    id bigint NOT NULL,
    title character varying(100) NOT NULL,
    subtitle character varying(200),
    image_url character varying(500) NOT NULL,
    link character varying(500),
    sort_order integer DEFAULT 0 NOT NULL,
    status smallint DEFAULT 1 NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.sys_banner OWNER TO fitness_user;

--
-- Name: TABLE sys_banner; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.sys_banner IS '轮播图管理';


--
-- Name: sys_banner_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.sys_banner_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_banner_id_seq OWNER TO fitness_user;

--
-- Name: sys_banner_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.sys_banner_id_seq OWNED BY public.sys_banner.id;


--
-- Name: sys_dict; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.sys_dict (
    id bigint NOT NULL,
    dict_name character varying(100) NOT NULL,
    dict_code character varying(100) NOT NULL,
    description text,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    sort_order integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.sys_dict OWNER TO fitness_user;

--
-- Name: TABLE sys_dict; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.sys_dict IS '数据字典表';


--
-- Name: COLUMN sys_dict.dict_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict.dict_name IS '字典名称';


--
-- Name: COLUMN sys_dict.dict_code; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict.dict_code IS '字典编码（唯一标识）';


--
-- Name: COLUMN sys_dict.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict.description IS '描述';


--
-- Name: COLUMN sys_dict.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict.status IS '状态：ACTIVE-启用，INACTIVE-禁用';


--
-- Name: COLUMN sys_dict.sort_order; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict.sort_order IS '排序号';


--
-- Name: COLUMN sys_dict.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict.deleted IS '逻辑删除';


--
-- Name: sys_dict_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.sys_dict_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_dict_id_seq OWNER TO fitness_user;

--
-- Name: sys_dict_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.sys_dict_id_seq OWNED BY public.sys_dict.id;


--
-- Name: sys_dict_item; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.sys_dict_item (
    id bigint NOT NULL,
    dict_id bigint NOT NULL,
    label character varying(200) NOT NULL,
    value character varying(200) NOT NULL,
    description text,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    sort_order integer DEFAULT 0,
    extra jsonb,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.sys_dict_item OWNER TO fitness_user;

--
-- Name: TABLE sys_dict_item; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.sys_dict_item IS '数据字典项表';


--
-- Name: COLUMN sys_dict_item.dict_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict_item.dict_id IS '所属字典ID';


--
-- Name: COLUMN sys_dict_item.label; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict_item.label IS '显示名称';


--
-- Name: COLUMN sys_dict_item.value; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict_item.value IS '实际值';


--
-- Name: COLUMN sys_dict_item.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict_item.description IS '描述';


--
-- Name: COLUMN sys_dict_item.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict_item.status IS '状态：ACTIVE-启用，INACTIVE-禁用';


--
-- Name: COLUMN sys_dict_item.sort_order; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict_item.sort_order IS '排序号';


--
-- Name: COLUMN sys_dict_item.extra; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict_item.extra IS '扩展属性（JSON格式）';


--
-- Name: COLUMN sys_dict_item.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_dict_item.deleted IS '逻辑删除';


--
-- Name: sys_dict_item_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.sys_dict_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_dict_item_id_seq OWNER TO fitness_user;

--
-- Name: sys_dict_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.sys_dict_item_id_seq OWNED BY public.sys_dict_item.id;


--
-- Name: sys_file; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.sys_file (
    id bigint NOT NULL,
    file_name character varying(255) NOT NULL,
    original_name character varying(255) NOT NULL,
    file_url character varying(500) NOT NULL,
    file_type character varying(50),
    file_size bigint DEFAULT 0,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    create_by bigint
);


ALTER TABLE public.sys_file OWNER TO fitness_user;

--
-- Name: TABLE sys_file; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.sys_file IS '文件记录表';


--
-- Name: COLUMN sys_file.file_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_file.file_name IS '存储文件名';


--
-- Name: COLUMN sys_file.original_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_file.original_name IS '原始文件名';


--
-- Name: COLUMN sys_file.file_url; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_file.file_url IS '文件访问URL';


--
-- Name: COLUMN sys_file.file_type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_file.file_type IS '文件类型';


--
-- Name: COLUMN sys_file.file_size; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_file.file_size IS '文件大小(字节)';


--
-- Name: COLUMN sys_file.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_file.deleted IS '软删除标志';


--
-- Name: COLUMN sys_file.create_by; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_file.create_by IS '创建人ID';


--
-- Name: sys_file_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.sys_file_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_file_id_seq OWNER TO fitness_user;

--
-- Name: sys_file_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.sys_file_id_seq OWNED BY public.sys_file.id;


--
-- Name: sys_permission; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.sys_permission (
    id bigint NOT NULL,
    permission_name character varying(50) NOT NULL,
    permission_code character varying(100) NOT NULL,
    type smallint DEFAULT 1 NOT NULL,
    parent_id bigint DEFAULT 0,
    path character varying(200),
    icon character varying(100),
    sort_order integer DEFAULT 0,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.sys_permission OWNER TO fitness_user;

--
-- Name: TABLE sys_permission; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.sys_permission IS '权限表';


--
-- Name: COLUMN sys_permission.permission_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_permission.permission_name IS '权限名称';


--
-- Name: COLUMN sys_permission.permission_code; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_permission.permission_code IS '权限编码';


--
-- Name: COLUMN sys_permission.type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_permission.type IS '类型: 1-菜单, 2-按钮, 3-接口';


--
-- Name: COLUMN sys_permission.parent_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_permission.parent_id IS '父权限ID';


--
-- Name: COLUMN sys_permission.path; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_permission.path IS '路由路径';


--
-- Name: COLUMN sys_permission.icon; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_permission.icon IS '图标';


--
-- Name: COLUMN sys_permission.sort_order; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_permission.sort_order IS '排序';


--
-- Name: COLUMN sys_permission.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_permission.deleted IS '软删除标志';


--
-- Name: sys_permission_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.sys_permission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_permission_id_seq OWNER TO fitness_user;

--
-- Name: sys_permission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.sys_permission_id_seq OWNED BY public.sys_permission.id;


--
-- Name: sys_role; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.sys_role (
    id bigint NOT NULL,
    role_name character varying(50) NOT NULL,
    role_code character varying(50) NOT NULL,
    description character varying(255),
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.sys_role OWNER TO fitness_user;

--
-- Name: TABLE sys_role; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.sys_role IS '角色表';


--
-- Name: COLUMN sys_role.role_name; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_role.role_name IS '角色名称';


--
-- Name: COLUMN sys_role.role_code; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_role.role_code IS '角色编码';


--
-- Name: COLUMN sys_role.description; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_role.description IS '角色描述';


--
-- Name: COLUMN sys_role.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_role.deleted IS '软删除标志';


--
-- Name: sys_role_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.sys_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_role_id_seq OWNER TO fitness_user;

--
-- Name: sys_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.sys_role_id_seq OWNED BY public.sys_role.id;


--
-- Name: sys_role_permission; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.sys_role_permission (
    id bigint NOT NULL,
    role_id bigint NOT NULL,
    permission_id bigint NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.sys_role_permission OWNER TO fitness_user;

--
-- Name: TABLE sys_role_permission; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.sys_role_permission IS '角色权限关联表';


--
-- Name: sys_role_permission_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.sys_role_permission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_role_permission_id_seq OWNER TO fitness_user;

--
-- Name: sys_role_permission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.sys_role_permission_id_seq OWNED BY public.sys_role_permission.id;


--
-- Name: sys_user; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.sys_user (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    phone character varying(20),
    email character varying(100),
    avatar character varying(500),
    status smallint DEFAULT 1 NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    points integer DEFAULT 0,
    nickname character varying(50),
    balance numeric(10,2) DEFAULT 0.00
);


ALTER TABLE public.sys_user OWNER TO fitness_user;

--
-- Name: TABLE sys_user; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.sys_user IS '用户表';


--
-- Name: COLUMN sys_user.status; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_user.status IS '状态: 0-禁用, 1-启用';


--
-- Name: COLUMN sys_user.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_user.deleted IS '软删除标志';


--
-- Name: COLUMN sys_user.points; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_user.points IS '积分余额';


--
-- Name: COLUMN sys_user.nickname; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_user.nickname IS '用户昵称/姓名，用于展示';


--
-- Name: COLUMN sys_user.balance; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.sys_user.balance IS '账户余额';


--
-- Name: sys_user_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.sys_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_user_id_seq OWNER TO fitness_user;

--
-- Name: sys_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.sys_user_id_seq OWNED BY public.sys_user.id;


--
-- Name: sys_user_role; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.sys_user_role (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.sys_user_role OWNER TO fitness_user;

--
-- Name: TABLE sys_user_role; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.sys_user_role IS '用户角色关联表';


--
-- Name: sys_user_role_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.sys_user_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_user_role_id_seq OWNER TO fitness_user;

--
-- Name: sys_user_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.sys_user_role_id_seq OWNED BY public.sys_user_role.id;


--
-- Name: user_fitness_profile; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.user_fitness_profile (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    height numeric(5,2),
    weight numeric(5,2),
    age integer,
    experience character varying(20),
    fitness_goal character varying(20),
    deleted boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    private_coach_id bigint,
    gender character varying(10)
);


ALTER TABLE public.user_fitness_profile OWNER TO fitness_user;

--
-- Name: TABLE user_fitness_profile; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.user_fitness_profile IS '用户健身档案表';


--
-- Name: COLUMN user_fitness_profile.user_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_fitness_profile.user_id IS '用户 ID';


--
-- Name: COLUMN user_fitness_profile.height; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_fitness_profile.height IS '身高 (cm)';


--
-- Name: COLUMN user_fitness_profile.weight; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_fitness_profile.weight IS '体重 (kg)';


--
-- Name: COLUMN user_fitness_profile.age; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_fitness_profile.age IS '年龄';


--
-- Name: COLUMN user_fitness_profile.experience; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_fitness_profile.experience IS '健身经验：BEGINNER-初学者，INTERMEDIATE-中级，ADVANCED-高级';


--
-- Name: COLUMN user_fitness_profile.fitness_goal; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_fitness_profile.fitness_goal IS '健身目标：WEIGHT_LOSS-减脂，MUSCLE_GAIN-增肌，BODY_SHAPING-塑形，ENDURANCE-增强体能，HEALTH-保持健康';


--
-- Name: COLUMN user_fitness_profile.deleted; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_fitness_profile.deleted IS '软删除标识';


--
-- Name: COLUMN user_fitness_profile.private_coach_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_fitness_profile.private_coach_id IS '专属教练ID（关联sys_user表）';


--
-- Name: COLUMN user_fitness_profile.gender; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_fitness_profile.gender IS '性别：MALE-男，FEMALE-女';


--
-- Name: user_fitness_profile_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.user_fitness_profile_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_fitness_profile_id_seq OWNER TO fitness_user;

--
-- Name: user_fitness_profile_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.user_fitness_profile_id_seq OWNED BY public.user_fitness_profile.id;


--
-- Name: user_membership; Type: TABLE; Schema: public; Owner: fitness_user
--

CREATE TABLE public.user_membership (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    membership_type character varying(50),
    start_time timestamp without time zone,
    expire_time timestamp without time zone,
    is_active boolean DEFAULT false,
    total_orders integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.user_membership OWNER TO fitness_user;

--
-- Name: TABLE user_membership; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON TABLE public.user_membership IS '用户会员信息表';


--
-- Name: COLUMN user_membership.user_id; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_membership.user_id IS '用户ID';


--
-- Name: COLUMN user_membership.membership_type; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_membership.membership_type IS '当前会员类型';


--
-- Name: COLUMN user_membership.start_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_membership.start_time IS '会员开始时间';


--
-- Name: COLUMN user_membership.expire_time; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_membership.expire_time IS '会员到期时间';


--
-- Name: COLUMN user_membership.is_active; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_membership.is_active IS '是否有效';


--
-- Name: COLUMN user_membership.total_orders; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON COLUMN public.user_membership.total_orders IS '累计购买次数';


--
-- Name: user_membership_id_seq; Type: SEQUENCE; Schema: public; Owner: fitness_user
--

CREATE SEQUENCE public.user_membership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_membership_id_seq OWNER TO fitness_user;

--
-- Name: user_membership_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fitness_user
--

ALTER SEQUENCE public.user_membership_id_seq OWNED BY public.user_membership.id;


--
-- Name: analysis_report id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.analysis_report ALTER COLUMN id SET DEFAULT nextval('public.analysis_report_id_seq'::regclass);


--
-- Name: chat_long_term_memory id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.chat_long_term_memory ALTER COLUMN id SET DEFAULT nextval('public.chat_long_term_memory_id_seq'::regclass);


--
-- Name: coach_detail id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_detail ALTER COLUMN id SET DEFAULT nextval('public.coach_detail_id_seq'::regclass);


--
-- Name: coach_notification id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_notification ALTER COLUMN id SET DEFAULT nextval('public.coach_notification_id_seq'::regclass);


--
-- Name: coach_package id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_package ALTER COLUMN id SET DEFAULT nextval('public.coach_package_id_seq'::regclass);


--
-- Name: coach_package_order_ext id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_package_order_ext ALTER COLUMN id SET DEFAULT nextval('public.coach_package_order_ext_id_seq'::regclass);


--
-- Name: coach_student id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_student ALTER COLUMN id SET DEFAULT nextval('public.coach_student_id_seq'::regclass);


--
-- Name: fitness_booking id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_booking ALTER COLUMN id SET DEFAULT nextval('public.fitness_booking_id_seq'::regclass);


--
-- Name: fitness_course id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_course ALTER COLUMN id SET DEFAULT nextval('public.fitness_course_id_seq'::regclass);


--
-- Name: fitness_course_session id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_course_session ALTER COLUMN id SET DEFAULT nextval('public.fitness_course_session_id_seq'::regclass);


--
-- Name: fitness_equipment id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_equipment ALTER COLUMN id SET DEFAULT nextval('public.fitness_equipment_id_seq'::regclass);


--
-- Name: fitness_equipment_repair id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_equipment_repair ALTER COLUMN id SET DEFAULT nextval('public.fitness_equipment_repair_id_seq'::regclass);


--
-- Name: fitness_equipment_type id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_equipment_type ALTER COLUMN id SET DEFAULT nextval('public.fitness_equipment_type_id_seq'::regclass);


--
-- Name: fitness_plan id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_plan ALTER COLUMN id SET DEFAULT nextval('public.fitness_plan_id_seq'::regclass);


--
-- Name: fitness_plan_detail id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_plan_detail ALTER COLUMN id SET DEFAULT nextval('public.fitness_plan_detail_id_seq'::regclass);


--
-- Name: fitness_private_coach_booking id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_private_coach_booking ALTER COLUMN id SET DEFAULT nextval('public.fitness_private_coach_booking_id_seq'::regclass);


--
-- Name: fitness_repair_record id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_repair_record ALTER COLUMN id SET DEFAULT nextval('public.fitness_repair_record_id_seq'::regclass);


--
-- Name: fitness_video_course id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_video_course ALTER COLUMN id SET DEFAULT nextval('public.fitness_video_course_id_seq'::regclass);


--
-- Name: knowledge_category id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.knowledge_category ALTER COLUMN id SET DEFAULT nextval('public.knowledge_category_id_seq'::regclass);


--
-- Name: knowledge_chunk id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.knowledge_chunk ALTER COLUMN id SET DEFAULT nextval('public.knowledge_chunk_id_seq'::regclass);


--
-- Name: knowledge_document id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.knowledge_document ALTER COLUMN id SET DEFAULT nextval('public.knowledge_document_id_seq'::regclass);


--
-- Name: membership_card id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_card ALTER COLUMN id SET DEFAULT nextval('public.membership_card_id_seq'::regclass);


--
-- Name: membership_card_content id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_card_content ALTER COLUMN id SET DEFAULT nextval('public.membership_card_content_id_seq'::regclass);


--
-- Name: membership_card_type id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_card_type ALTER COLUMN id SET DEFAULT nextval('public.membership_card_type_id_seq'::regclass);


--
-- Name: membership_order_ext id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_order_ext ALTER COLUMN id SET DEFAULT nextval('public.membership_order_ext_id_seq'::regclass);


--
-- Name: membership_order_old id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_order_old ALTER COLUMN id SET DEFAULT nextval('public.membership_order_id_seq'::regclass);


--
-- Name: orders id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.orders ALTER COLUMN id SET DEFAULT nextval('public.orders_id_seq'::regclass);


--
-- Name: product id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.product ALTER COLUMN id SET DEFAULT nextval('public.product_id_seq'::regclass);


--
-- Name: product_order_ext id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.product_order_ext ALTER COLUMN id SET DEFAULT nextval('public.product_order_ext_id_seq'::regclass);


--
-- Name: product_order_old id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.product_order_old ALTER COLUMN id SET DEFAULT nextval('public.product_order_id_seq'::regclass);


--
-- Name: sys_announcement id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_announcement ALTER COLUMN id SET DEFAULT nextval('public.sys_announcement_id_seq'::regclass);


--
-- Name: sys_banner id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_banner ALTER COLUMN id SET DEFAULT nextval('public.sys_banner_id_seq'::regclass);


--
-- Name: sys_dict id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_dict ALTER COLUMN id SET DEFAULT nextval('public.sys_dict_id_seq'::regclass);


--
-- Name: sys_dict_item id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_dict_item ALTER COLUMN id SET DEFAULT nextval('public.sys_dict_item_id_seq'::regclass);


--
-- Name: sys_file id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_file ALTER COLUMN id SET DEFAULT nextval('public.sys_file_id_seq'::regclass);


--
-- Name: sys_permission id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_permission ALTER COLUMN id SET DEFAULT nextval('public.sys_permission_id_seq'::regclass);


--
-- Name: sys_role id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_role ALTER COLUMN id SET DEFAULT nextval('public.sys_role_id_seq'::regclass);


--
-- Name: sys_role_permission id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_role_permission ALTER COLUMN id SET DEFAULT nextval('public.sys_role_permission_id_seq'::regclass);


--
-- Name: sys_user id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_user ALTER COLUMN id SET DEFAULT nextval('public.sys_user_id_seq'::regclass);


--
-- Name: sys_user_role id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_user_role ALTER COLUMN id SET DEFAULT nextval('public.sys_user_role_id_seq'::regclass);


--
-- Name: user_fitness_profile id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.user_fitness_profile ALTER COLUMN id SET DEFAULT nextval('public.user_fitness_profile_id_seq'::regclass);


--
-- Name: user_membership id; Type: DEFAULT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.user_membership ALTER COLUMN id SET DEFAULT nextval('public.user_membership_id_seq'::regclass);


--
-- Name: analysis_report analysis_report_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.analysis_report
    ADD CONSTRAINT analysis_report_pkey PRIMARY KEY (id);


--
-- Name: chat_long_term_memory chat_long_term_memory_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.chat_long_term_memory
    ADD CONSTRAINT chat_long_term_memory_pkey PRIMARY KEY (id);


--
-- Name: chat_long_term_memory chat_long_term_memory_user_id_memory_key_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.chat_long_term_memory
    ADD CONSTRAINT chat_long_term_memory_user_id_memory_key_key UNIQUE (user_id, memory_key);


--
-- Name: chat_memory_store chat_memory_store_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.chat_memory_store
    ADD CONSTRAINT chat_memory_store_pkey PRIMARY KEY (id);


--
-- Name: chat_message chat_message_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.chat_message
    ADD CONSTRAINT chat_message_pkey PRIMARY KEY (id);


--
-- Name: chat_session chat_session_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.chat_session
    ADD CONSTRAINT chat_session_pkey PRIMARY KEY (id);


--
-- Name: coach_detail coach_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_detail
    ADD CONSTRAINT coach_detail_pkey PRIMARY KEY (id);


--
-- Name: coach_detail coach_detail_user_id_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_detail
    ADD CONSTRAINT coach_detail_user_id_key UNIQUE (user_id);


--
-- Name: coach_notification coach_notification_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_notification
    ADD CONSTRAINT coach_notification_pkey PRIMARY KEY (id);


--
-- Name: coach_package_order_ext coach_package_order_ext_order_id_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_package_order_ext
    ADD CONSTRAINT coach_package_order_ext_order_id_key UNIQUE (order_id);


--
-- Name: coach_package_order_ext coach_package_order_ext_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_package_order_ext
    ADD CONSTRAINT coach_package_order_ext_pkey PRIMARY KEY (id);


--
-- Name: coach_package coach_package_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_package
    ADD CONSTRAINT coach_package_pkey PRIMARY KEY (id);


--
-- Name: coach_student coach_student_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_student
    ADD CONSTRAINT coach_student_pkey PRIMARY KEY (id);


--
-- Name: fitness_booking fitness_booking_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_booking
    ADD CONSTRAINT fitness_booking_pkey PRIMARY KEY (id);


--
-- Name: fitness_course fitness_course_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_course
    ADD CONSTRAINT fitness_course_pkey PRIMARY KEY (id);


--
-- Name: fitness_course_session fitness_course_session_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_course_session
    ADD CONSTRAINT fitness_course_session_pkey PRIMARY KEY (id);


--
-- Name: fitness_equipment fitness_equipment_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_equipment
    ADD CONSTRAINT fitness_equipment_pkey PRIMARY KEY (id);


--
-- Name: fitness_equipment_repair fitness_equipment_repair_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_equipment_repair
    ADD CONSTRAINT fitness_equipment_repair_pkey PRIMARY KEY (id);


--
-- Name: fitness_equipment_type fitness_equipment_type_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_equipment_type
    ADD CONSTRAINT fitness_equipment_type_pkey PRIMARY KEY (id);


--
-- Name: fitness_equipment_type fitness_equipment_type_type_code_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_equipment_type
    ADD CONSTRAINT fitness_equipment_type_type_code_key UNIQUE (type_code);


--
-- Name: fitness_plan_detail fitness_plan_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_plan_detail
    ADD CONSTRAINT fitness_plan_detail_pkey PRIMARY KEY (id);


--
-- Name: fitness_plan fitness_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_plan
    ADD CONSTRAINT fitness_plan_pkey PRIMARY KEY (id);


--
-- Name: fitness_private_coach_booking fitness_private_coach_booking_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_private_coach_booking
    ADD CONSTRAINT fitness_private_coach_booking_pkey PRIMARY KEY (id);


--
-- Name: fitness_repair_record fitness_repair_record_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_repair_record
    ADD CONSTRAINT fitness_repair_record_pkey PRIMARY KEY (id);


--
-- Name: fitness_video_course fitness_video_course_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_video_course
    ADD CONSTRAINT fitness_video_course_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: knowledge_category knowledge_category_code_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.knowledge_category
    ADD CONSTRAINT knowledge_category_code_key UNIQUE (code);


--
-- Name: knowledge_category knowledge_category_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.knowledge_category
    ADD CONSTRAINT knowledge_category_pkey PRIMARY KEY (id);


--
-- Name: knowledge_chunk knowledge_chunk_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.knowledge_chunk
    ADD CONSTRAINT knowledge_chunk_pkey PRIMARY KEY (id);


--
-- Name: knowledge_document knowledge_document_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.knowledge_document
    ADD CONSTRAINT knowledge_document_pkey PRIMARY KEY (id);


--
-- Name: membership_card_content membership_card_content_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_card_content
    ADD CONSTRAINT membership_card_content_pkey PRIMARY KEY (id);


--
-- Name: membership_card membership_card_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_card
    ADD CONSTRAINT membership_card_pkey PRIMARY KEY (id);


--
-- Name: membership_card_type membership_card_type_code_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_card_type
    ADD CONSTRAINT membership_card_type_code_key UNIQUE (code);


--
-- Name: membership_card_type membership_card_type_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_card_type
    ADD CONSTRAINT membership_card_type_pkey PRIMARY KEY (id);


--
-- Name: membership_order_ext membership_order_ext_order_id_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_order_ext
    ADD CONSTRAINT membership_order_ext_order_id_key UNIQUE (order_id);


--
-- Name: membership_order_ext membership_order_ext_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_order_ext
    ADD CONSTRAINT membership_order_ext_pkey PRIMARY KEY (id);


--
-- Name: membership_order_old membership_order_order_no_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_order_old
    ADD CONSTRAINT membership_order_order_no_key UNIQUE (order_no);


--
-- Name: membership_order_old membership_order_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_order_old
    ADD CONSTRAINT membership_order_pkey PRIMARY KEY (id);


--
-- Name: orders orders_order_no_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_order_no_key UNIQUE (order_no);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: product product_code_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_code_key UNIQUE (code);


--
-- Name: product_order_ext product_order_ext_order_id_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.product_order_ext
    ADD CONSTRAINT product_order_ext_order_id_key UNIQUE (order_id);


--
-- Name: product_order_ext product_order_ext_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.product_order_ext
    ADD CONSTRAINT product_order_ext_pkey PRIMARY KEY (id);


--
-- Name: product_order_old product_order_order_no_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.product_order_old
    ADD CONSTRAINT product_order_order_no_key UNIQUE (order_no);


--
-- Name: product_order_old product_order_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.product_order_old
    ADD CONSTRAINT product_order_pkey PRIMARY KEY (id);


--
-- Name: product product_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);


--
-- Name: sys_announcement sys_announcement_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_announcement
    ADD CONSTRAINT sys_announcement_pkey PRIMARY KEY (id);


--
-- Name: sys_banner sys_banner_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_banner
    ADD CONSTRAINT sys_banner_pkey PRIMARY KEY (id);


--
-- Name: sys_dict sys_dict_dict_code_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_dict
    ADD CONSTRAINT sys_dict_dict_code_key UNIQUE (dict_code);


--
-- Name: sys_dict_item sys_dict_item_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_dict_item
    ADD CONSTRAINT sys_dict_item_pkey PRIMARY KEY (id);


--
-- Name: sys_dict sys_dict_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_dict
    ADD CONSTRAINT sys_dict_pkey PRIMARY KEY (id);


--
-- Name: sys_file sys_file_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_file
    ADD CONSTRAINT sys_file_pkey PRIMARY KEY (id);


--
-- Name: sys_permission sys_permission_permission_code_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_permission
    ADD CONSTRAINT sys_permission_permission_code_key UNIQUE (permission_code);


--
-- Name: sys_permission sys_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_permission
    ADD CONSTRAINT sys_permission_pkey PRIMARY KEY (id);


--
-- Name: sys_role_permission sys_role_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_role_permission
    ADD CONSTRAINT sys_role_permission_pkey PRIMARY KEY (id);


--
-- Name: sys_role_permission sys_role_permission_role_id_permission_id_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_role_permission
    ADD CONSTRAINT sys_role_permission_role_id_permission_id_key UNIQUE (role_id, permission_id);


--
-- Name: sys_role sys_role_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_role
    ADD CONSTRAINT sys_role_pkey PRIMARY KEY (id);


--
-- Name: sys_role sys_role_role_code_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_role
    ADD CONSTRAINT sys_role_role_code_key UNIQUE (role_code);


--
-- Name: sys_user sys_user_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_user
    ADD CONSTRAINT sys_user_pkey PRIMARY KEY (id);


--
-- Name: sys_user_role sys_user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT sys_user_role_pkey PRIMARY KEY (id);


--
-- Name: sys_user_role sys_user_role_user_id_role_id_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT sys_user_role_user_id_role_id_key UNIQUE (user_id, role_id);


--
-- Name: sys_user sys_user_username_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_user
    ADD CONSTRAINT sys_user_username_key UNIQUE (username);


--
-- Name: fitness_equipment uk_equipment_name; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_equipment
    ADD CONSTRAINT uk_equipment_name UNIQUE (equipment_name);


--
-- Name: CONSTRAINT uk_equipment_name ON fitness_equipment; Type: COMMENT; Schema: public; Owner: fitness_user
--

COMMENT ON CONSTRAINT uk_equipment_name ON public.fitness_equipment IS '器械名称唯一约束';


--
-- Name: user_fitness_profile user_fitness_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.user_fitness_profile
    ADD CONSTRAINT user_fitness_profile_pkey PRIMARY KEY (id);


--
-- Name: user_fitness_profile user_fitness_profile_user_id_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.user_fitness_profile
    ADD CONSTRAINT user_fitness_profile_user_id_key UNIQUE (user_id);


--
-- Name: user_membership user_membership_pkey; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.user_membership
    ADD CONSTRAINT user_membership_pkey PRIMARY KEY (id);


--
-- Name: user_membership user_membership_user_id_key; Type: CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.user_membership
    ADD CONSTRAINT user_membership_user_id_key UNIQUE (user_id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_analysis_report_create_by; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_analysis_report_create_by ON public.analysis_report USING btree (create_by);


--
-- Name: idx_analysis_report_generate_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_analysis_report_generate_time ON public.analysis_report USING btree (generate_time DESC);


--
-- Name: idx_analysis_report_is_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_analysis_report_is_deleted ON public.analysis_report USING btree (is_deleted);


--
-- Name: idx_analysis_report_type; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_analysis_report_type ON public.analysis_report USING btree (analysis_type);


--
-- Name: idx_booking_session_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_booking_session_id ON public.fitness_booking USING btree (session_id);


--
-- Name: idx_card_content_card_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_card_content_card_id ON public.membership_card_content USING btree (card_id);


--
-- Name: idx_chat_long_term_memory_type; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_chat_long_term_memory_type ON public.chat_long_term_memory USING btree (memory_type);


--
-- Name: idx_chat_long_term_memory_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_chat_long_term_memory_user_id ON public.chat_long_term_memory USING btree (user_id);


--
-- Name: idx_chat_message_created_at; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_chat_message_created_at ON public.chat_message USING btree (created_at);


--
-- Name: idx_chat_message_session_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_chat_message_session_id ON public.chat_message USING btree (session_id);


--
-- Name: idx_chat_session_created_at; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_chat_session_created_at ON public.chat_session USING btree (created_at);


--
-- Name: idx_chat_session_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_chat_session_user_id ON public.chat_session USING btree (user_id);


--
-- Name: idx_cn_coach_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_cn_coach_id ON public.coach_notification USING btree (coach_id);


--
-- Name: idx_cn_coach_unread; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_cn_coach_unread ON public.coach_notification USING btree (coach_id, is_read) WHERE ((is_read = false) AND (deleted = false));


--
-- Name: idx_cn_create_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_cn_create_time ON public.coach_notification USING btree (create_time DESC);


--
-- Name: idx_coach_detail_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_coach_detail_deleted ON public.coach_detail USING btree (deleted);


--
-- Name: idx_coach_detail_real_name; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_coach_detail_real_name ON public.coach_detail USING btree (real_name);


--
-- Name: idx_coach_detail_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_coach_detail_user_id ON public.coach_detail USING btree (user_id);


--
-- Name: idx_cp_coach_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_cp_coach_id ON public.coach_package USING btree (coach_id);


--
-- Name: idx_cp_package_code; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_cp_package_code ON public.coach_package USING btree (package_code);


--
-- Name: idx_cp_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_cp_status ON public.coach_package USING btree (status);


--
-- Name: idx_cpoe_coach_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_cpoe_coach_id ON public.coach_package_order_ext USING btree (coach_id);


--
-- Name: idx_cs_coach_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_cs_coach_id ON public.coach_student USING btree (coach_id);


--
-- Name: idx_cs_coach_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_cs_coach_status ON public.coach_student USING btree (coach_id, status) WHERE (deleted = false);


--
-- Name: idx_cs_member_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_cs_member_id ON public.coach_student USING btree (member_id);


--
-- Name: idx_dict_item_dict_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_dict_item_dict_id ON public.sys_dict_item USING btree (dict_id);


--
-- Name: idx_dict_item_value; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_dict_item_value ON public.sys_dict_item USING btree (value);


--
-- Name: idx_fitness_booking_course_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_booking_course_id ON public.fitness_booking USING btree (course_id);


--
-- Name: idx_fitness_booking_course_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_booking_course_status ON public.fitness_booking USING btree (course_id, status) WHERE (deleted = false);


--
-- Name: idx_fitness_booking_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_booking_deleted ON public.fitness_booking USING btree (deleted);


--
-- Name: idx_fitness_booking_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_booking_status ON public.fitness_booking USING btree (status);


--
-- Name: idx_fitness_booking_status_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_booking_status_deleted ON public.fitness_booking USING btree (status, deleted);


--
-- Name: idx_fitness_booking_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_booking_user_id ON public.fitness_booking USING btree (user_id);


--
-- Name: idx_fitness_booking_user_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_booking_user_time ON public.fitness_booking USING btree (user_id, booking_time) WHERE (deleted = false);


--
-- Name: idx_fitness_course_category; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_course_category ON public.fitness_course USING btree (category);


--
-- Name: idx_fitness_course_category_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_course_category_deleted ON public.fitness_course USING btree (category, deleted);


--
-- Name: idx_fitness_course_coach_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_course_coach_id ON public.fitness_course USING btree (coach_id);


--
-- Name: idx_fitness_course_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_course_deleted ON public.fitness_course USING btree (deleted);


--
-- Name: idx_fitness_course_start_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_course_start_time ON public.fitness_course USING btree (start_time);


--
-- Name: idx_fitness_course_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_course_status ON public.fitness_course USING btree (status);


--
-- Name: idx_fitness_course_total_booking_count; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_course_total_booking_count ON public.fitness_course USING btree (total_booking_count);


--
-- Name: idx_fitness_equipment_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_equipment_deleted ON public.fitness_equipment USING btree (deleted);


--
-- Name: idx_fitness_equipment_repair_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_equipment_repair_deleted ON public.fitness_equipment_repair USING btree (deleted);


--
-- Name: idx_fitness_equipment_repair_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_equipment_repair_status ON public.fitness_equipment_repair USING btree (status);


--
-- Name: idx_fitness_equipment_repair_status_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_equipment_repair_status_deleted ON public.fitness_equipment_repair USING btree (status, deleted);


--
-- Name: idx_fitness_equipment_repair_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_equipment_repair_user_id ON public.fitness_equipment_repair USING btree (user_id);


--
-- Name: idx_fitness_equipment_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_equipment_status ON public.fitness_equipment USING btree (status);


--
-- Name: idx_fitness_plan_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_plan_deleted ON public.fitness_plan USING btree (deleted);


--
-- Name: idx_fitness_plan_detail_day_index; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_plan_detail_day_index ON public.fitness_plan_detail USING btree (day_index);


--
-- Name: idx_fitness_plan_detail_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_plan_detail_deleted ON public.fitness_plan_detail USING btree (deleted);


--
-- Name: idx_fitness_plan_detail_plan_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_plan_detail_plan_id ON public.fitness_plan_detail USING btree (plan_id);


--
-- Name: idx_fitness_plan_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_plan_status ON public.fitness_plan USING btree (status);


--
-- Name: idx_fitness_plan_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_plan_user_id ON public.fitness_plan USING btree (user_id);


--
-- Name: idx_fitness_plan_user_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_plan_user_time ON public.fitness_plan USING btree (user_id, create_time DESC);


--
-- Name: idx_fitness_repair_record_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_repair_record_deleted ON public.fitness_repair_record USING btree (deleted);


--
-- Name: idx_fitness_repair_record_handler_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_repair_record_handler_id ON public.fitness_repair_record USING btree (handler_id);


--
-- Name: idx_fitness_repair_record_repair_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_fitness_repair_record_repair_id ON public.fitness_repair_record USING btree (repair_id);


--
-- Name: idx_knowledge_category_code; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_knowledge_category_code ON public.knowledge_category USING btree (code);


--
-- Name: idx_knowledge_category_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_knowledge_category_deleted ON public.knowledge_category USING btree (deleted);


--
-- Name: idx_knowledge_chunk_content_fts; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_knowledge_chunk_content_fts ON public.knowledge_chunk USING gin (to_tsvector('simple'::regconfig, content));


--
-- Name: idx_knowledge_chunk_content_hash; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_knowledge_chunk_content_hash ON public.knowledge_chunk USING btree (content_hash);


--
-- Name: idx_knowledge_chunk_document_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_knowledge_chunk_document_id ON public.knowledge_chunk USING btree (document_id);


--
-- Name: idx_knowledge_chunk_embedding; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_knowledge_chunk_embedding ON public.knowledge_chunk USING hnsw (embedding public.vector_cosine_ops);


--
-- Name: idx_knowledge_document_category_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_knowledge_document_category_id ON public.knowledge_document USING btree (category_id);


--
-- Name: idx_knowledge_document_create_by; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_knowledge_document_create_by ON public.knowledge_document USING btree (create_by);


--
-- Name: idx_knowledge_document_create_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_knowledge_document_create_time ON public.knowledge_document USING btree (create_time);


--
-- Name: idx_knowledge_document_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_knowledge_document_deleted ON public.knowledge_document USING btree (deleted);


--
-- Name: idx_knowledge_document_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_knowledge_document_status ON public.knowledge_document USING btree (status);


--
-- Name: idx_membership_card_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_membership_card_status ON public.membership_card USING btree (status);


--
-- Name: idx_membership_card_type_code; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_membership_card_type_code ON public.membership_card USING btree (type_code);


--
-- Name: idx_membership_card_type_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_membership_card_type_id ON public.membership_card USING btree (type_id);


--
-- Name: idx_membership_order_no; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_membership_order_no ON public.membership_order_old USING btree (order_no);


--
-- Name: idx_membership_order_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_membership_order_status ON public.membership_order_old USING btree (status);


--
-- Name: idx_membership_order_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_membership_order_user_id ON public.membership_order_old USING btree (user_id);


--
-- Name: idx_moe_card_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_moe_card_id ON public.membership_order_ext USING btree (card_id);


--
-- Name: idx_orders_create_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_orders_create_time ON public.orders USING btree (create_time);


--
-- Name: idx_orders_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_orders_status ON public.orders USING btree (status);


--
-- Name: idx_orders_type; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_orders_type ON public.orders USING btree (order_type);


--
-- Name: idx_orders_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_orders_user_id ON public.orders USING btree (user_id);


--
-- Name: idx_pcb_booking_date; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_pcb_booking_date ON public.fitness_private_coach_booking USING btree (booking_date);


--
-- Name: idx_pcb_coach_date; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_pcb_coach_date ON public.fitness_private_coach_booking USING btree (coach_id, booking_date);


--
-- Name: idx_pcb_coach_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_pcb_coach_id ON public.fitness_private_coach_booking USING btree (coach_id);


--
-- Name: idx_pcb_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_pcb_user_id ON public.fitness_private_coach_booking USING btree (user_id);


--
-- Name: idx_poe_product_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_poe_product_id ON public.product_order_ext USING btree (product_id);


--
-- Name: idx_product_order_create_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_product_order_create_time ON public.product_order_old USING btree (create_time);


--
-- Name: idx_product_order_created_at_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_product_order_created_at_status ON public.product_order_old USING btree (create_time, status) WHERE ((status)::text = ANY ((ARRAY['PAID'::character varying, 'SHIPPED'::character varying, 'COMPLETED'::character varying])::text[]));


--
-- Name: idx_product_order_expire_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_product_order_expire_time ON public.product_order_old USING btree (expire_time);


--
-- Name: idx_product_order_pickup_code; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_product_order_pickup_code ON public.product_order_old USING btree (pickup_code);


--
-- Name: idx_product_order_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_product_order_status ON public.product_order_old USING btree (status);


--
-- Name: idx_product_order_status_created_at; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_product_order_status_created_at ON public.product_order_old USING btree (status, create_time);


--
-- Name: idx_product_order_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_product_order_user_id ON public.product_order_old USING btree (user_id);


--
-- Name: idx_repair_equipment_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_repair_equipment_id ON public.fitness_equipment_repair USING btree (equipment_id);


--
-- Name: idx_session_course_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_session_course_id ON public.fitness_course_session USING btree (course_id);


--
-- Name: idx_session_date; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_session_date ON public.fitness_course_session USING btree (session_date);


--
-- Name: idx_session_unique; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE UNIQUE INDEX idx_session_unique ON public.fitness_course_session USING btree (course_id, session_date) WHERE (deleted = false);


--
-- Name: idx_sys_announcement_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_announcement_deleted ON public.sys_announcement USING btree (deleted);


--
-- Name: idx_sys_announcement_publish_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_announcement_publish_time ON public.sys_announcement USING btree (publish_time);


--
-- Name: idx_sys_announcement_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_announcement_status ON public.sys_announcement USING btree (status);


--
-- Name: idx_sys_announcement_type; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_announcement_type ON public.sys_announcement USING btree (type);


--
-- Name: idx_sys_banner_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_banner_deleted ON public.sys_banner USING btree (deleted);


--
-- Name: idx_sys_banner_sort_order; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_banner_sort_order ON public.sys_banner USING btree (sort_order);


--
-- Name: idx_sys_banner_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_banner_status ON public.sys_banner USING btree (status);


--
-- Name: idx_sys_dict_code; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_dict_code ON public.sys_dict USING btree (dict_code);


--
-- Name: idx_sys_dict_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_dict_deleted ON public.sys_dict USING btree (deleted);


--
-- Name: idx_sys_dict_item_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_dict_item_deleted ON public.sys_dict_item USING btree (deleted);


--
-- Name: idx_sys_dict_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_dict_status ON public.sys_dict USING btree (status);


--
-- Name: idx_sys_file_create_by; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_file_create_by ON public.sys_file USING btree (create_by);


--
-- Name: idx_sys_file_create_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_file_create_time ON public.sys_file USING btree (create_time);


--
-- Name: idx_sys_file_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_file_deleted ON public.sys_file USING btree (deleted);


--
-- Name: idx_sys_file_file_name; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_file_file_name ON public.sys_file USING btree (file_name);


--
-- Name: idx_sys_permission_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_permission_deleted ON public.sys_permission USING btree (deleted);


--
-- Name: idx_sys_permission_parent_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_permission_parent_id ON public.sys_permission USING btree (parent_id);


--
-- Name: idx_sys_permission_type; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_permission_type ON public.sys_permission USING btree (type);


--
-- Name: idx_sys_role_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_role_deleted ON public.sys_role USING btree (deleted);


--
-- Name: idx_sys_role_permission_permission_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_role_permission_permission_id ON public.sys_role_permission USING btree (permission_id);


--
-- Name: idx_sys_role_permission_role_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_role_permission_role_id ON public.sys_role_permission USING btree (role_id);


--
-- Name: idx_sys_role_role_code; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_role_role_code ON public.sys_role USING btree (role_code);


--
-- Name: idx_sys_user_create_time; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_user_create_time ON public.sys_user USING btree (create_time);


--
-- Name: idx_sys_user_deleted; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_user_deleted ON public.sys_user USING btree (deleted);


--
-- Name: idx_sys_user_nickname; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_user_nickname ON public.sys_user USING btree (nickname);


--
-- Name: idx_sys_user_phone; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_user_phone ON public.sys_user USING btree (phone);


--
-- Name: idx_sys_user_role_role_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_user_role_role_id ON public.sys_user_role USING btree (role_id);


--
-- Name: idx_sys_user_role_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_user_role_user_id ON public.sys_user_role USING btree (user_id);


--
-- Name: idx_sys_user_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_user_status ON public.sys_user USING btree (status);


--
-- Name: idx_sys_user_username; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_sys_user_username ON public.sys_user USING btree (username);


--
-- Name: idx_user_balance; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_user_balance ON public.sys_user USING btree (balance);


--
-- Name: idx_user_fitness_profile_private_coach_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_user_fitness_profile_private_coach_id ON public.user_fitness_profile USING btree (private_coach_id);


--
-- Name: idx_user_fitness_profile_user_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_user_fitness_profile_user_id ON public.user_fitness_profile USING btree (user_id);


--
-- Name: idx_video_course_category; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_video_course_category ON public.fitness_video_course USING btree (category);


--
-- Name: idx_video_course_coach_id; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_video_course_coach_id ON public.fitness_video_course USING btree (coach_id);


--
-- Name: idx_video_course_status; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE INDEX idx_video_course_status ON public.fitness_video_course USING btree (status);


--
-- Name: uk_cs_member_coach_active; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE UNIQUE INDEX uk_cs_member_coach_active ON public.coach_student USING btree (member_id, coach_id) WHERE (((status)::text = 'ACTIVE'::text) AND (deleted = false));


--
-- Name: uk_fitness_booking_user_session_active; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE UNIQUE INDEX uk_fitness_booking_user_session_active ON public.fitness_booking USING btree (user_id, session_id) WHERE ((session_id IS NOT NULL) AND (deleted = false) AND (status = ANY (ARRAY[0, 1])));


--
-- Name: uk_pcb_user_coach_date_time_active; Type: INDEX; Schema: public; Owner: fitness_user
--

CREATE UNIQUE INDEX uk_pcb_user_coach_date_time_active ON public.fitness_private_coach_booking USING btree (user_id, coach_id, booking_date, start_time) WHERE ((status = ANY (ARRAY[0, 1])) AND (deleted = false));


--
-- Name: chat_long_term_memory update_chat_long_term_memory_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_chat_long_term_memory_update_time BEFORE UPDATE ON public.chat_long_term_memory FOR EACH ROW EXECUTE FUNCTION public.update_chat_long_term_memory_update_time();


--
-- Name: coach_notification update_coach_notification_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_coach_notification_update_time BEFORE UPDATE ON public.coach_notification FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: coach_package update_coach_package_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_coach_package_update_time BEFORE UPDATE ON public.coach_package FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: coach_student update_coach_student_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_coach_student_update_time BEFORE UPDATE ON public.coach_student FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: fitness_booking update_fitness_booking_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_fitness_booking_update_time BEFORE UPDATE ON public.fitness_booking FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: fitness_course update_fitness_course_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_fitness_course_update_time BEFORE UPDATE ON public.fitness_course FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: fitness_equipment_repair update_fitness_equipment_repair_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_fitness_equipment_repair_update_time BEFORE UPDATE ON public.fitness_equipment_repair FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: fitness_equipment update_fitness_equipment_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_fitness_equipment_update_time BEFORE UPDATE ON public.fitness_equipment FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: fitness_plan_detail update_fitness_plan_detail_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_fitness_plan_detail_update_time BEFORE UPDATE ON public.fitness_plan_detail FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: fitness_plan update_fitness_plan_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_fitness_plan_update_time BEFORE UPDATE ON public.fitness_plan FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: fitness_private_coach_booking update_fitness_private_coach_booking_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_fitness_private_coach_booking_update_time BEFORE UPDATE ON public.fitness_private_coach_booking FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: fitness_repair_record update_fitness_repair_record_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_fitness_repair_record_update_time BEFORE UPDATE ON public.fitness_repair_record FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: fitness_video_course update_fitness_video_course_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_fitness_video_course_update_time BEFORE UPDATE ON public.fitness_video_course FOR EACH ROW EXECUTE FUNCTION public.update_fitness_video_course_update_time();


--
-- Name: knowledge_category update_knowledge_category_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_knowledge_category_update_time BEFORE UPDATE ON public.knowledge_category FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: knowledge_document update_knowledge_document_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_knowledge_document_update_time BEFORE UPDATE ON public.knowledge_document FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: orders update_orders_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_orders_update_time BEFORE UPDATE ON public.orders FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: sys_announcement update_sys_announcement_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_sys_announcement_update_time BEFORE UPDATE ON public.sys_announcement FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: sys_permission update_sys_permission_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_sys_permission_update_time BEFORE UPDATE ON public.sys_permission FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: sys_role update_sys_role_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_sys_role_update_time BEFORE UPDATE ON public.sys_role FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: sys_user update_sys_user_update_time; Type: TRIGGER; Schema: public; Owner: fitness_user
--

CREATE TRIGGER update_sys_user_update_time BEFORE UPDATE ON public.sys_user FOR EACH ROW EXECUTE FUNCTION public.update_update_time_column();


--
-- Name: membership_card_content fk_card_content_card; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_card_content
    ADD CONSTRAINT fk_card_content_card FOREIGN KEY (card_id) REFERENCES public.membership_card(id) ON DELETE CASCADE;


--
-- Name: coach_notification fk_cn_coach_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_notification
    ADD CONSTRAINT fk_cn_coach_id FOREIGN KEY (coach_id) REFERENCES public.sys_user(id) ON DELETE CASCADE;


--
-- Name: coach_notification fk_cn_student_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_notification
    ADD CONSTRAINT fk_cn_student_id FOREIGN KEY (student_id) REFERENCES public.sys_user(id) ON DELETE CASCADE;


--
-- Name: coach_detail fk_coach_detail_user_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_detail
    ADD CONSTRAINT fk_coach_detail_user_id FOREIGN KEY (user_id) REFERENCES public.sys_user(id) ON DELETE CASCADE;


--
-- Name: coach_package fk_cp_coach_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_package
    ADD CONSTRAINT fk_cp_coach_id FOREIGN KEY (coach_id) REFERENCES public.sys_user(id);


--
-- Name: coach_package_order_ext fk_cpoe_order_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_package_order_ext
    ADD CONSTRAINT fk_cpoe_order_id FOREIGN KEY (order_id) REFERENCES public.orders(id);


--
-- Name: coach_student fk_cs_coach_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_student
    ADD CONSTRAINT fk_cs_coach_id FOREIGN KEY (coach_id) REFERENCES public.sys_user(id) ON DELETE CASCADE;


--
-- Name: coach_student fk_cs_member_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.coach_student
    ADD CONSTRAINT fk_cs_member_id FOREIGN KEY (member_id) REFERENCES public.sys_user(id) ON DELETE CASCADE;


--
-- Name: sys_dict_item fk_dict_item_dict; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_dict_item
    ADD CONSTRAINT fk_dict_item_dict FOREIGN KEY (dict_id) REFERENCES public.sys_dict(id) ON DELETE CASCADE;


--
-- Name: fitness_booking fk_fitness_booking_course_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_booking
    ADD CONSTRAINT fk_fitness_booking_course_id FOREIGN KEY (course_id) REFERENCES public.fitness_course(id) ON DELETE CASCADE;


--
-- Name: fitness_booking fk_fitness_booking_user_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_booking
    ADD CONSTRAINT fk_fitness_booking_user_id FOREIGN KEY (user_id) REFERENCES public.sys_user(id) ON DELETE CASCADE;


--
-- Name: fitness_course fk_fitness_course_coach_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_course
    ADD CONSTRAINT fk_fitness_course_coach_id FOREIGN KEY (coach_id) REFERENCES public.sys_user(id) ON DELETE RESTRICT;


--
-- Name: fitness_equipment_repair fk_fitness_equipment_repair_user_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_equipment_repair
    ADD CONSTRAINT fk_fitness_equipment_repair_user_id FOREIGN KEY (user_id) REFERENCES public.sys_user(id) ON DELETE SET NULL;


--
-- Name: fitness_plan_detail fk_fitness_plan_detail_plan_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_plan_detail
    ADD CONSTRAINT fk_fitness_plan_detail_plan_id FOREIGN KEY (plan_id) REFERENCES public.fitness_plan(id) ON DELETE CASCADE;


--
-- Name: fitness_plan fk_fitness_plan_user_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_plan
    ADD CONSTRAINT fk_fitness_plan_user_id FOREIGN KEY (user_id) REFERENCES public.sys_user(id) ON DELETE CASCADE;


--
-- Name: fitness_repair_record fk_fitness_repair_record_handler_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_repair_record
    ADD CONSTRAINT fk_fitness_repair_record_handler_id FOREIGN KEY (handler_id) REFERENCES public.sys_user(id) ON DELETE SET NULL;


--
-- Name: fitness_repair_record fk_fitness_repair_record_repair_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_repair_record
    ADD CONSTRAINT fk_fitness_repair_record_repair_id FOREIGN KEY (repair_id) REFERENCES public.fitness_equipment_repair(id) ON DELETE CASCADE;


--
-- Name: knowledge_chunk fk_knowledge_chunk_document_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.knowledge_chunk
    ADD CONSTRAINT fk_knowledge_chunk_document_id FOREIGN KEY (document_id) REFERENCES public.knowledge_document(id) ON DELETE CASCADE;


--
-- Name: knowledge_document fk_knowledge_document_category_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.knowledge_document
    ADD CONSTRAINT fk_knowledge_document_category_id FOREIGN KEY (category_id) REFERENCES public.knowledge_category(id) ON DELETE SET NULL;


--
-- Name: membership_card fk_membership_card_type; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_card
    ADD CONSTRAINT fk_membership_card_type FOREIGN KEY (type_id) REFERENCES public.membership_card_type(id);


--
-- Name: membership_order_ext fk_moe_order_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_order_ext
    ADD CONSTRAINT fk_moe_order_id FOREIGN KEY (order_id) REFERENCES public.orders(id);


--
-- Name: membership_order_old fk_order_card; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_order_old
    ADD CONSTRAINT fk_order_card FOREIGN KEY (card_id) REFERENCES public.membership_card(id);


--
-- Name: membership_order_old fk_order_user; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.membership_order_old
    ADD CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES public.sys_user(id);


--
-- Name: orders fk_orders_user_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fk_orders_user_id FOREIGN KEY (user_id) REFERENCES public.sys_user(id);


--
-- Name: fitness_private_coach_booking fk_pcb_coach_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_private_coach_booking
    ADD CONSTRAINT fk_pcb_coach_id FOREIGN KEY (coach_id) REFERENCES public.sys_user(id) ON DELETE CASCADE;


--
-- Name: fitness_private_coach_booking fk_pcb_user_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_private_coach_booking
    ADD CONSTRAINT fk_pcb_user_id FOREIGN KEY (user_id) REFERENCES public.sys_user(id) ON DELETE CASCADE;


--
-- Name: product_order_ext fk_poe_order_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.product_order_ext
    ADD CONSTRAINT fk_poe_order_id FOREIGN KEY (order_id) REFERENCES public.orders(id);


--
-- Name: fitness_equipment_repair fk_repair_equipment; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.fitness_equipment_repair
    ADD CONSTRAINT fk_repair_equipment FOREIGN KEY (equipment_id) REFERENCES public.fitness_equipment(id) ON DELETE SET NULL;


--
-- Name: sys_role_permission fk_sys_role_permission_permission_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_role_permission
    ADD CONSTRAINT fk_sys_role_permission_permission_id FOREIGN KEY (permission_id) REFERENCES public.sys_permission(id) ON DELETE CASCADE;


--
-- Name: sys_role_permission fk_sys_role_permission_role_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_role_permission
    ADD CONSTRAINT fk_sys_role_permission_role_id FOREIGN KEY (role_id) REFERENCES public.sys_role(id) ON DELETE CASCADE;


--
-- Name: sys_user_role fk_sys_user_role_role_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT fk_sys_user_role_role_id FOREIGN KEY (role_id) REFERENCES public.sys_role(id) ON DELETE CASCADE;


--
-- Name: sys_user_role fk_sys_user_role_user_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT fk_sys_user_role_user_id FOREIGN KEY (user_id) REFERENCES public.sys_user(id) ON DELETE CASCADE;


--
-- Name: user_fitness_profile fk_user_fitness_profile_private_coach_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.user_fitness_profile
    ADD CONSTRAINT fk_user_fitness_profile_private_coach_id FOREIGN KEY (private_coach_id) REFERENCES public.sys_user(id);


--
-- Name: user_fitness_profile fk_user_fitness_profile_user_id; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.user_fitness_profile
    ADD CONSTRAINT fk_user_fitness_profile_user_id FOREIGN KEY (user_id) REFERENCES public.sys_user(id);


--
-- Name: user_membership fk_user_membership_user; Type: FK CONSTRAINT; Schema: public; Owner: fitness_user
--

ALTER TABLE ONLY public.user_membership
    ADD CONSTRAINT fk_user_membership_user FOREIGN KEY (user_id) REFERENCES public.sys_user(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: fitness_user
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;


--
-- PostgreSQL database dump complete
--

\unrestrict mVX1ZeuwZbwXhcr34wNZbqntm3Manenfn6olAvjhieL1zcRC3fa0cVpWcZziABM

