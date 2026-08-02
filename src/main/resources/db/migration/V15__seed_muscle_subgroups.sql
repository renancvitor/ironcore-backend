INSERT INTO muscle_subgroups (
    muscle_group_id,
    code,
    display_name,
    active,
    sort_order
)
VALUES
    -- Peitoral
    (
        (SELECT id FROM muscle_groups WHERE code = 'CHEST'),
        'UPPER_CHEST',
        'Peitoral superior',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'CHEST'),
        'MID_CHEST',
        'Peitoral médio',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'CHEST'),
        'LOWER_CHEST',
        'Peitoral inferior',
        TRUE,
        30
    ),

    -- Costas
    (
        (SELECT id FROM muscle_groups WHERE code = 'BACK'),
        'LATISSIMUS_DORSI',
        'Latíssimo do dorso',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'BACK'),
        'TERES_MAJOR',
        'Redondo maior',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'BACK'),
        'RHOMBOIDS',
        'Romboides',
        TRUE,
        30
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'BACK'),
        'UPPER_TRAPEZIUS',
        'Trapézio superior',
        TRUE,
        40
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'BACK'),
        'MIDDLE_TRAPEZIUS',
        'Trapézio médio',
        TRUE,
        50
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'BACK'),
        'LOWER_TRAPEZIUS',
        'Trapézio inferior',
        TRUE,
        60
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'BACK'),
        'ERECTOR_SPINAE',
        'Eretores da coluna',
        TRUE,
        70
    ),

    -- Ombros
    (
        (SELECT id FROM muscle_groups WHERE code = 'SHOULDERS'),
        'ANTERIOR_DELTOID',
        'Deltoide anterior',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'SHOULDERS'),
        'LATERAL_DELTOID',
        'Deltoide lateral',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'SHOULDERS'),
        'POSTERIOR_DELTOID',
        'Deltoide posterior',
        TRUE,
        30
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'SHOULDERS'),
        'SUPRASPINATUS',
        'Supraespinal',
        TRUE,
        40
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'SHOULDERS'),
        'INFRASPINATUS',
        'Infraespinal',
        TRUE,
        50
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'SHOULDERS'),
        'TERES_MINOR',
        'Redondo menor',
        TRUE,
        60
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'SHOULDERS'),
        'SUBSCAPULARIS',
        'Subescapular',
        TRUE,
        70
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'SHOULDERS'),
        'SERRATUS_ANTERIOR',
        'Serrátil anterior',
        TRUE,
        80
    ),

    -- Bíceps
    (
        (SELECT id FROM muscle_groups WHERE code = 'BICEPS'),
        'BICEPS_LONG_HEAD',
        'Cabeça longa do bíceps',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'BICEPS'),
        'BICEPS_SHORT_HEAD',
        'Cabeça curta do bíceps',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'BICEPS'),
        'BRACHIALIS',
        'Braquial',
        TRUE,
        30
    ),

    -- Antebraços
    (
        (SELECT id FROM muscle_groups WHERE code = 'FOREARMS'),
        'FOREARM_FLEXORS',
        'Flexores do antebraço',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'FOREARMS'),
        'FOREARM_EXTENSORS',
        'Extensores do antebraço',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'FOREARMS'),
        'BRACHIORADIALIS',
        'Braquiorradial',
        TRUE,
        30
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'FOREARMS'),
        'PRONATOR_TERES',
        'Pronador redondo',
        TRUE,
        40
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'FOREARMS'),
        'PRONATOR_QUADRATUS',
        'Pronador quadrado',
        TRUE,
        50
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'FOREARMS'),
        'SUPINATOR',
        'Supinador',
        TRUE,
        60
    ),

    -- Tríceps
    (
        (SELECT id FROM muscle_groups WHERE code = 'TRICEPS'),
        'TRICEPS_LONG_HEAD',
        'Cabeça longa do tríceps',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'TRICEPS'),
        'TRICEPS_LATERAL_HEAD',
        'Cabeça lateral do tríceps',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'TRICEPS'),
        'TRICEPS_MEDIAL_HEAD',
        'Cabeça medial do tríceps',
        TRUE,
        30
    ),

    -- Quadríceps
    (
        (SELECT id FROM muscle_groups WHERE code = 'QUADRICEPS'),
        'RECTUS_FEMORIS',
        'Reto femoral',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'QUADRICEPS'),
        'VASTUS_LATERALIS',
        'Vasto lateral',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'QUADRICEPS'),
        'VASTUS_MEDIALIS',
        'Vasto medial',
        TRUE,
        30
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'QUADRICEPS'),
        'VASTUS_INTERMEDIUS',
        'Vasto intermédio',
        TRUE,
        40
    ),

    -- Posteriores de coxa
    (
        (SELECT id FROM muscle_groups WHERE code = 'HAMSTRINGS'),
        'BICEPS_FEMORIS',
        'Bíceps femoral',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'HAMSTRINGS'),
        'SEMITENDINOSUS',
        'Semitendíneo',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'HAMSTRINGS'),
        'SEMIMEMBRANOSUS',
        'Semimembranáceo',
        TRUE,
        30
    ),

    -- Adutores
    (
        (SELECT id FROM muscle_groups WHERE code = 'ADDUCTORS'),
        'ADDUCTOR_MAGNUS',
        'Adutor magno',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'ADDUCTORS'),
        'ADDUCTOR_LONGUS',
        'Adutor longo',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'ADDUCTORS'),
        'ADDUCTOR_BREVIS',
        'Adutor curto',
        TRUE,
        30
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'ADDUCTORS'),
        'GRACILIS',
        'Grácil',
        TRUE,
        40
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'ADDUCTORS'),
        'PECTINEUS',
        'Pectíneo',
        TRUE,
        50
    ),

    -- Flexores do quadril
    (
        (SELECT id FROM muscle_groups WHERE code = 'HIP_FLEXORS'),
        'ILIOPSOAS',
        'Iliopsoas',
        TRUE,
        10
    ),

    -- Glúteos
    (
        (SELECT id FROM muscle_groups WHERE code = 'GLUTES'),
        'GLUTEUS_MAXIMUS',
        'Glúteo máximo',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'GLUTES'),
        'GLUTEUS_MEDIUS',
        'Glúteo médio',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'GLUTES'),
        'GLUTEUS_MINIMUS',
        'Glúteo mínimo',
        TRUE,
        30
    ),

    -- Panturrilhas e canelas
    (
        (SELECT id FROM muscle_groups WHERE code = 'LOWER_LEGS'),
        'GASTROCNEMIUS',
        'Gastrocnêmio',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'LOWER_LEGS'),
        'SOLEUS',
        'Sóleo',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'LOWER_LEGS'),
        'TIBIALIS_ANTERIOR',
        'Tibial anterior',
        TRUE,
        30
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'LOWER_LEGS'),
        'FIBULARIS_MUSCLES',
        'Músculos fibulares',
        TRUE,
        40
    ),

    -- Abdômen
    (
        (SELECT id FROM muscle_groups WHERE code = 'ABS'),
        'RECTUS_ABDOMINIS',
        'Reto abdominal',
        TRUE,
        10
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'ABS'),
        'OBLIQUES',
        'Oblíquos',
        TRUE,
        20
    ),
    (
        (SELECT id FROM muscle_groups WHERE code = 'ABS'),
        'TRANSVERSE_ABDOMINIS',
        'Transverso abdominal',
        TRUE,
        30
    );
