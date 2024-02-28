/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012.
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.istt.service.dto;

import java.util.EnumSet;

/**
 * Type of SMS to indicate Mo/Mt procedure.
 *
 * @author baranowb
 */
public enum ErrorCode {
    SUCCESS(0),

    UNKNOWN_SUBSCRIBER(1),
    UNDEFINED_SUBSCRIBER(2),
    ILLEGAL_SUBSCRIBER(3),
    TELESERVICE_NOT_PROVISIONED(4),
    CALL_BARRED(5),
    CUG_REJECTED(6),
    FACILITY_NOT_SUPPORTED(7),
    ABSENT_SUBSCRIBER(8),
    ABSENT_SUBSCRIBER_MWDSET(9),
    SENDING_SM_FAILED(10),
    MESSAGE_QUEUE_FULL(11),
    SYSTEM_FAILURE(12),
    DATA_MISSING(13),
    UNEXPECTED_DATA(14),

    ERROR_IN_MS(15),
    MS_NOT_EQUIPPED(16),
    MEMORY_FULL(17),
    SC_CONGESTION(18),
    MS_NOT_REG_IN_SC(19),
    INVALID_SME_ADDRESS(20),
    UNKNOWN_SMC(21),
    ILLEGAL_EQUIPMENT(22),
    USER_BUSY(23),
    // TODO XXX: table is weird
    // Power off (24–47: CDMA network reserved). 24-27
    // Reserved.   25-29
    //
    HLR_MESSAGE_DECODING_ERROR(30),
    MSC_MESSAGE_DECODIG_ERROR(31),
    INFORM_SC_MESSAGE_DECODING_ERROR(32),
    // 'Not obtain enough routing information' ??? what?
    NOT_ENOUGH_ROUTING_INFORMATION(33),
    UNEXPECTED_DATA_FROM_HLR(34),
    UNEXPECTED_DATA_FROM_MSC(35),
    UNKNOWN_ERROR_FROM_MSC(36),
    UNKNOWN_ERROR_FROM_HLR(37),
    // Reserved(38-39),
    BAD_TYPE_OF_NUMBER(38),
    BAD_NPI(39),
    /** HLR does not send acknowledgment after routing request is sent(40), */
    HLR_REJECT_AFTER_ROUTING_INFO(40),
    /** HLR does not send acknowledgment after the message of setting flag bit is sent(41), */
    HLR_REJECT_AFTER_FLAG_BIT(41),
    // Reserved(42-44),
    RESERVED_42(42),
    RESERVED_43(43),
    RESERVED_44(44),

    MAP_SERVER_VERSION_ERROR(45),
    HLR_VERSION_NEGOTIATION_ERROR(46),
    RESERVED_47(47),
    /** MSC does not send acknowledgment(48), */
    MSC_NO_ACK(48),
    /** HLR does not send acknowledgment(49), */
    HLR_NO_ACK(49),
    /** GIW does not send acknowledgment(50), */
    GIW_NO_ACK(50),
    MSC_REFUSES_SM(51),
    HLR_REFUSES_SM(52),
    GIW_REFUSES_SM(53),
    // TODO: XXX table is weird here again
    // No response from the SGSN (54–63),
    NO_RESPONSE_FROM_SGSN(54),

    SGSN_REGUSES_SM(55),
    HLR_SYSTEM_ERROR(56),
    MSC_SYSTEM_ERROR(57),
    SGSN_SYSTEM_ERROR(58),
    RESERVED_59(59),
    RESERVED_60(60),
    /** Delivery error due to MAP Server flow control(61), */
    MAP_SERVER_FLOW_CONTROLL_ERROR(61),
    /** Delivery error due to MTI Server flow control(62), */
    MTI_SERVER_FLOW_CONTROLL_ERROR(62),
    /** SCCP of DSP or STP unable to send the message(63), */
    SCCP_UNABLE_TO_SEND(63),
    INTERFACE_NO_DELIVERY_AUTHORITY(64),
    /** GW does not send acknowledgment(65), */
    GW_NO_ACK(65),
    /** Temporary interface error (deregistered or not log in)(66), */
    TEMPORARY_INTERFACE_ERROR(66),
    INVALID_INTERFACE(67),
    /** Service interface does not send acknowledgment(68), */
    SERVICE_INTERFACE_NO_ACK(68),
    RESERVED_69(69),
    /** Exceeding maximum of messages in L2CacheDaemon specified by license(70), */
    EXCEEDED_L2CacheDaemon_MAX_MESSAGE(70), // TODO:XXX ???
    /**
     * Deletion of SMs to be imported to the database because of disconnection between the SMSC and
     * L2CacheDaemon(71),
     */
    DELETION_OF_SM_ON_SMSC_L2CacheDaemon_DISCONNECT(71),
    /** Exceeding the maximum L2CacheDaemon capacity specified by license(72), */
    EXCEEDED_L2CacheDaemon_CAPACITY(72), // TODO:XXX ??? //TODO: XXX: again ?
    /** Some messages in L2CacheDaemon are the same as the messages in memory(73), */
    L2CacheDaemon_MESSAGE_DUPLICATION(73),
    L2CacheDaemon_DB_UNAVAILABLE(74),
    L2CacheDaemon_CONGESTED(75),
    /** Error when exporting SMs from memory to L2CacheDaemon(76), */
    ERROR_ON_EXPORTING_SM_FROM_MEMORY_TO_L2CacheDaemon(76),
    /** POOL may be full in message delivery(77), */
    POOL_FULL_IN_DELIVERY(77),
    /** MT speed exceeds the License threshold by 120%(78), */
    MT_SPEED_EXCEEDED(78),
    /**
     * Number of entities that exceed the maximum submission number(Delivery of the SM failed and the
     * SM is deleted(79),
     */
    MAX_SM_DELIVERY_RETRY_EXCEEDED(79),
    // Reserved(80 - 127),
    RESERVED_80(80),
    RESERVED_81(81),
    RESERVED_82(82),
    RESERVED_83(83),
    RESERVED_84(84),
    RESERVED_85(85),
    RESERVED_86(86),
    RESERVED_87(87),
    RESERVED_88(88),
    RESERVED_89(89),
    RESERVED_90(90),
    RESERVED_91(91),
    RESERVED_92(92),
    RESERVED_93(93),
    RESERVED_94(94),
    RESERVED_95(95),
    RESERVED_96(96),
    RESERVED_97(97),
    RESERVED_98(98),
    RESERVED_99(99),
    RESERVED_100(100),
    RESERVED_101(101),
    RESERVED_102(102),
    RESERVED_103(103),
    RESERVED_104(104),
    RESERVED_105(105),
    RESERVED_106(106),
    RESERVED_107(107),
    RESERVED_108(108),
    RESERVED_109(109),
    RESERVED_110(110),
    RESERVED_111(111),
    RESERVED_112(112),
    RESERVED_113(113),
    RESERVED_114(114),
    RESERVED_115(115),
    RESERVED_116(116),
    RESERVED_117(117),
    RESERVED_118(118),
    RESERVED_119(119),
    RESERVED_120(120),
    RESERVED_121(121),
    RESERVED_122(122),
    RESERVED_123(123),
    RESERVED_124(124),
    RESERVED_125(125),
    RESERVED_126(126),
    RESERVED_127(127),

    /** Teleservice facility interaction not supported(128), */
    TELESERVICE_FACILITY_INTERACTION_NOT_SUPPORTED(128),
    SM_TYPE_0_NOT_SUPPORTED(129),
    CANNOT_REPLACE_SM(128),
    // Reserved(131–142),
    RESERVED_131(131),
    RESERVED_132(132),
    RESERVED_133(133),
    RESERVED_134(134),
    RESERVED_135(135),
    RESERVED_136(136),
    RESERVED_137(137),
    RESERVED_138(138),
    RESERVED_139(139),
    RESERVED_140(140),
    RESERVED_141(141),
    RESERVED_142(142),
    UNSPECIFIED_TP_PID_ERROR(143),
    DCS_NOT_SUPPORTED(144),
    SM_TYPE_NOT_SUPPORTED(145), // TODO: XXX :again? there is SM_TYPE_0_NOT_SUPPORTED />
    // Reserved(146–158),
    RESERVED_146(146),
    RESERVED_147(147),
    RESERVED_148(148),
    RESERVED_149(149),
    RESERVED_150(150),
    RESERVED_151(151),
    RESERVED_152(152),
    RESERVED_153(153),
    RESERVED_154(154),
    RESERVED_155(155),
    RESERVED_156(156),
    RESERVED_157(157),
    RESERVED_158(158),
    UNSPECIFIED_TP_DCS_ERROR(159),
    OPERATION_NOT_EXECUTED(160),
    // Reserved(161–174),
    RESERVED_161(161),
    RESERVED_162(162),
    RESERVED_163(163),
    RESERVED_164(164),
    RESERVED_165(165),
    RESERVED_166(166),
    RESERVED_167(167),
    RESERVED_168(168),
    RESERVED_169(169),
    RESERVED_170(170),
    RESERVED_171(171),
    RESERVED_172(172),
    RESERVED_173(173),
    RESERVED_174(174),
    TPDU_NOT_SUPPORTED(176),
    // Reserved(177–191),
    RESERVED_177(177),
    RESERVED_178(178),
    RESERVED_179(179),
    RESERVED_180(180),
    RESERVED_181(181),
    RESERVED_182(182),
    RESERVED_183(183),
    RESERVED_184(184),
    RESERVED_185(185),
    RESERVED_186(186),
    RESERVED_187(187),
    RESERVED_188(188),
    RESERVED_189(189),
    RESERVED_190(190),
    RESERVED_191(191),

    SC_BUSY(192),
    NO_SC_SPECIFIED(193),
    SC_SYSTEM_ERROR(194),
    INVALID_SME_ADDRESS_2(195), // TODO: XXX: duplicate!
    DESTINATION_SME_PROHIBITED(196),
    // Reserved(197-207),
    RESERVED_197(197),
    RESERVED_198(198),
    RESERVED_199(199),
    RESERVED_200(200),
    RESERVED_201(201),
    RESERVED_202(202),
    RESERVED_203(203),
    RESERVED_204(204),
    RESERVED_205(205),
    RESERVED_206(206),
    RESERVED_207(207),
    SIM_SMS_STORAGE_IS_FULL(208),
    /** No SMS storage capability in SIM(209), */
    SIM_HAS_NO_SMS_STORAGE(209),
    ERROR_IN_MS_2(210), // TODO: XXX: duplicate!
    ESME_MEMORY_OVERFLOW(211),
    /** Reserved(212-223), */
    RESERVED_212(212),
    RESERVED_213(213),
    RESERVED_214(214),
    RESERVED_215(215),
    RESERVED_216(216),
    RESERVED_217(217),
    RESERVED_218(218),
    RESERVED_219(219),
    RESERVED_220(220),
    RESERVED_221(221),
    RESERVED_222(222),
    RESERVED_223(223),
    /** Values specific to an application(224-254), */
    OCS_ACCESS_NOT_GRANTED(224),
    MPROC_ACCESS_NOT_GRANTED(225),
    MPROC_SRI_REQUEST_DROP(226),
    MPROC_PRE_DELIVERY_DROP(227),
    VALIDITY_PERIOD_EXPIRED(228),
    REJECT_INCOMING(229),
    REJECT_INCOMING_MPROC(230),
    APP_SPECIFIC_231(231),
    APP_SPECIFIC_232(232),
    APP_SPECIFIC_233(233),
    APP_SPECIFIC_234(234),
    APP_SPECIFIC_235(235),
    APP_SPECIFIC_236(236),
    APP_SPECIFIC_237(237),
    APP_SPECIFIC_238(238),
    APP_SPECIFIC_239(239),
    APP_SPECIFIC_240(240),
    APP_SPECIFIC_241(241),
    APP_SPECIFIC_242(242),
    APP_SPECIFIC_243(243),
    APP_SPECIFIC_244(244),
    APP_SPECIFIC_245(245),
    APP_SPECIFIC_246(246),
    APP_SPECIFIC_247(247),
    APP_SPECIFIC_248(248),
    APP_SPECIFIC_249(249),
    APP_SPECIFIC_250(250),
    APP_SPECIFIC_251(251),
    APP_SPECIFIC_252(252),
    APP_SPECIFIC_253(253),
    APP_SPECIFIC_254(254),
    UNSPECIFIED_ERROR_CAUSE(255);

    private static final EnumSet<ErrorCode> ENUM_SET = EnumSet.allOf(ErrorCode.class);
    private int code;

    ErrorCode(int v) {
        this.code = v;
    }

    public int getCode() {
        return code;
    }

    public String getCodeText() {
        return String.format("%03d", code);
    }

    public static ErrorCode fromInt(int code) {
        for (ErrorCode el : ENUM_SET) {
            if (el.code == code) return el;
        }
        throw new IllegalArgumentException("The '" + code + "' is not a valid value!");
    }
}
