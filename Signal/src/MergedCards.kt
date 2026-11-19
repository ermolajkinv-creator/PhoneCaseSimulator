package signal

// Published album numbers are permanent. Retired numbers resolve to the surviving card.
private val mergedAlbumNumbers = mapOf(
    109 to 199, // Samsung Galaxy Z Flip6 Olympic Edition
    5464 to 188, // Motorola Razr+ 2024
    6459 to 4858, // Sharp Aquos sense5G
    6574 to 6573, // Asus ROG Phone II ZS660KL
    6578 to 6575, // Asus Zenfone 6 ZS630KL
    6582 to 6581, // ASUS Zenfone Max Plus M2 (ZB634KL)
    6584 to 6583, // Asus Zenfone Max Shot ZB634KL
    7652 to 7651, // Xiaomi Redmi Note 8 2019
    7787 to 7786, // Asus ROG Phone ZS600KL
    7799 to 7798, // ASUS ZenFone Max M1 (ZB556KL)
    7804 to 7802, // ASUS ZenFone Max Pro M2 (ZB631KL)
    9351 to 9350, // Asus Zenfone 3 Zoom ZE553KL
    9353 to 9352, // Asus Zenfone 3s Max ZC521TL
    9358 to 9357, // Asus Zenfone 4 Max Pro ZC554KL
    9359 to 9355, // Asus Zenfone 4 Max ZC520KL
    9362 to 9360, // Asus Zenfone 4 Pro ZS551KL
    9365 to 9364, // Asus Zenfone 4 Selfie Pro ZD552KL
    9367 to 9363, // Asus Zenfone 4 Selfie ZD553KL
    9368 to 9354, // Asus Zenfone 4 ZE554KL
    9370 to 9369, // Asus Zenfone AR ZS571KL
    9373 to 9372, // Asus Zenfone Live ZB501KL
    9375 to 9374, // ASUS ZenFone Max Plus M1 (ZB570TL)
    9380 to 9378, // Asus Zenfone V V520KL
    9801 to 9780, // Huawei Honor 6C Pro
    9802 to 9781, // Huawei Honor 7X
    9803 to 9782, // Huawei Honor 8 Lite
    9804 to 9783, // Huawei Honor 8 Pro
    9805 to 9784, // Huawei Honor 9
    9806 to 9785, // Huawei Honor 9 Lite
    9809 to 9786, // Huawei Honor View 10
    10703 to 10700, // Alcatel Pixi 4 6
    10727 to 10726, // Apple iPhone SE (2016)
    10779 to 10778, // Asus Zenfone 3 Laser ZC551KL
    10781 to 10780, // Asus Zenfone 3 Max ZC520TL
    10784 to 10783, // Asus Zenfone 3 Ultra ZU680KL
    10785 to 10774, // Asus Zenfone 3 ZE520KL
    10787 to 9371, // ASUS ZenFone Go (ZB552KL)
    11164 to 11127, // Huawei Honor 5C
    11166 to 11128, // Huawei Honor 6X
    11167 to 11130, // Huawei Honor 8
    11169 to 11133, // Huawei Honor Magic
    11170 to 11134, // Huawei Honor Note 8
    11171 to 6086, // Huawei Honor Play 5
    11172 to 11135, // Huawei Honor V8
    12217 to 12214, // Asus Zenfone 2 Laser ZE500KG
    12220 to 12212, // Asus Zenfone 2 ZE500CL
    12226 to 12225, // Asus Zenfone C ZC451CG
    12231 to 12230, // Asus Zenfone Selfie ZD551KL
    12234 to 12232, // Asus Zenfone Zoom ZX551ML
    12606 to 12553, // Huawei Honor 7
    12843 to 12842, // Micromax Canvas Fire 4G+ Q412
    13140 to 13139, // Samsung Galaxy J1 Ace SM-J110
    13640 to 58, // BlackBerry Classic (Q20)
    13926 to 13923, // HTC One E8
    13949 to 13893, // Huawei Honor 4 Play
    13950 to 13894, // Huawei Honor 4X
    13951 to 13895, // Huawei Honor 6
    13952 to 13896, // Huawei Honor 6 Plus
    14402 to 14399, // Samsung Galaxy Ace Style SM-G310HN
    14591 to 14593, // Xiaomi Hongmi (Redmi) 1s
    15085 to 15018, // Huawei Honor 3
    15086 to 15019, // Huawei Honor 3C
    15400 to 15399, // Oppo R1 R829T
    15721 to 15722, // Xiaomi Red Rice (Hongmi, Redmi)
    16081 to 16011, // Huawei Honor 2
    16805 to 16804, // Apple iPhone 4 Verizon
    18398 to 18397, // Samsung S3650 Corby
    18781 to 18780, // Samsung i8510 INNOV8
    18925 to 10, // Apple iPhone
    20477 to 15722, // Xiaomi Hongmi
    22282 to 10788, // Asus Zenfone Go 5.0 LTE T500
    24776 to 7797, // Asus Zenfone Max M1 ZB555KL
    24918 to 6889, // HUAWEI Honor 20 Pro
    24920 to 6895, // HUAWEI Honor 8A Pro
    24921 to 8217, // HUAWEI Honor 8X Max
    24922 to 423, // HUAWEI Honor 9x Pro
    25180 to 14409, // Samsung Galaxy Core Max
    26376 to 11168, // Honor 8 Smart
    26385 to 9808, // Honor V9 Play
    26450 to 8205, // HUAWEI Honor 10
    26451 to 8206, // HUAWEI Honor 10 Lite
    26453 to 6888, // HUAWEI Honor 20
    26455 to 6890, // HUAWEI Honor 20i
    26459 to 26375, // HUAWEI Honor 6 Play
    26462 to 8213, // HUAWEI Honor 7S
    26463 to 6893, // HUAWEI Honor 8A
    26464 to 8215, // HUAWEI Honor 8C
    26469 to 4643, // HUAWEI Honor 9x Lite
    26470 to 6900, // HUAWEI Honor Magic 2 3D
    26471 to 8221, // HUAWEI Honor Note 10
    26472 to 8222, // HUAWEI Honor Play
    26473 to 6901, // HUAWEI Honor Play 3
    26474 to 6902, // HUAWEI Honor Play 3e
    26475 to 4644, // Huawei Honor Play 4
    26478 to 6903, // HUAWEI Honor Play 8A
    26479 to 4648, // HUAWEI Honor Play 9A
    26480 to 16896, // Huawei Honor U8860
    26482 to 26384, // HUAWEI Honor V9
    26483 to 8224, // HUAWEI Honor View 20
    27527 to 27526, // M-Tech TEZ4G
    28808 to 11894, // VERTEX Impress In Touch 3G
    28809 to 10474, // VERTEX Impress Lion 3G
    35631 to 9650, // FLY Cirrus 11 FS517
    35632 to 11007, // FLY Cirrus 3 FS506
    35633 to 11008, // FLY Cirrus 4 FS507
    35634 to 9655, // FLY Cirrus 6 FS508
    35635 to 11009, // FLY Cirrus 7 FS511
    35657 to 11022, // FLY Nimbus 10 FS512
    35659 to 11025, // FLY Nimbus 7 FS505
    35660 to 11026, // FLY Nimbus 8 FS454
    35661 to 11028, // FLY Stratus 3 FS404
    35662 to 9676, // FLY Stratus 8 FS408
    50056 to 11132, // Huawei Honor Holly 3
    50386 to 24908, // Huawei Honor Holly 4 Plus
    50623 to 13488, // Acer Liquid Jade S S56
    50627 to 13492, // Acer Liquid Z5 Z150
    51455 to 11010, // FLY Cirrus 8 FS514
    51665 to 12603, // Honor 4A
    51669 to 26481, // Honor V10
    51679 to 26378, // Huawei Honor Bee 2
    51680 to 26379, // Huawei Honor Holly 4
    52589 to 5490, // PHILIPS S6210 Essence 10
    53471 to 14593, // Xiaomi HM 1S
    60359 to 5491, // Philips S6310 Essence 20
    60371 to 1119, // Apple iPhone SE 3
    60385 to 365, // Apple iPhone SE 2
    60484 to 9850, // InFocus A1 M500
    60485 to 9851, // InFocus A1s M505
    60786 to 14470, // Sonim XP7 XP7700
)

internal fun canonicalId(id: Int): Int = mergedAlbumNumbers[id + 1]?.minus(1) ?: id

internal val activePhoneIds = phones.indices.filter { canonicalId(it) == it }
