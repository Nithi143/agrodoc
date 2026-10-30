-- ==============================================================================
-- AgroDoc Disease Seed Data
-- MySQL 8.0
-- Run AFTER schema.sql to populate the disease_info and disease_remedies tables.
-- ==============================================================================
-- Usage:
--   mysql -u root -p agrodoc_db < database/seed_diseases.sql
-- ==============================================================================
-- NOTE: The disease_name values in disease_info must exactly match the labels
-- returned by your chosen external AI API. Update these values accordingly
-- after completing the AI API evaluation in Phase 5.
-- ==============================================================================

USE agrodoc_db;

-- Prevent duplicate inserts if script is re-run
-- (IGNORE silently skips rows that violate the UNIQUE constraint on disease_name)

-- ==============================================================================
-- DISEASE: Tomato Early Blight
-- ==============================================================================
INSERT IGNORE INTO disease_info
    (disease_name, scientific_name, description, symptoms, causes, prevention_tips, severity_level)
VALUES (
    'Tomato Early Blight',
    'Alternaria solani',
    'Early Blight is one of the most common fungal diseases affecting tomato plants worldwide. It causes significant defoliation and yield loss when conditions are warm and humid.',
    'Dark brown to black circular spots with concentric rings (target-board pattern) on older lower leaves. Spots are surrounded by a yellow halo. Severe infection causes leaves to turn yellow and drop prematurely.',
    'Caused by the fungus Alternaria solani. Spreads through infected plant debris, soil, and water splashing. Favoured by warm temperatures (24–29°C) and high humidity with periods of leaf wetness.',
    'Use certified disease-free seeds. Rotate crops — avoid planting tomatoes in the same location for 2–3 years. Remove and destroy infected plant debris. Apply mulch to reduce soil splash. Avoid overhead irrigation. Ensure good air circulation between plants.',
    'MED'
);

-- Early Blight Remedies
INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Copper-based fungicide (Copper Oxychloride)',
    'Mix 3g per litre of water. Spray evenly on all leaf surfaces (upper and lower). Apply every 7–10 days, starting at first sign of symptoms. Avoid application in direct midday sun.',
    'Copper products are certified for organic use. Overuse can lead to copper toxicity in soil.',
    1
FROM disease_info WHERE disease_name = 'Tomato Early Blight'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Copper-based fungicide (Copper Oxychloride)'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Neem Oil Spray',
    'Mix 5ml neem oil with 1 litre water and a few drops of liquid soap as emulsifier. Spray on foliage every 7 days. Best applied in early morning or evening.',
    'Neem oil also deters insect pests. Do not apply during flowering as it may affect pollinators.',
    2
FROM disease_info WHERE disease_name = 'Tomato Early Blight'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Neem Oil Spray'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Chlorothalonil (Daconil)',
    'Mix according to label instructions (typically 2g/litre). Apply as a protective spray before disease appears or at first symptoms. Repeat every 7–14 days.',
    'Wear protective clothing and gloves. Observe pre-harvest interval (PHI) on product label. Do not apply within 7 days of harvest.',
    1
FROM disease_info WHERE disease_name = 'Tomato Early Blight'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Chlorothalonil (Daconil)'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Mancozeb (Dithane M-45)',
    'Mix 2.5g per litre of water. Spray all plant surfaces thoroughly. Apply every 7–10 days as a preventive or at first sign of infection.',
    'Avoid repeated application of the same fungicide class to prevent resistance. Rotate with different mode-of-action products.',
    2
FROM disease_info WHERE disease_name = 'Tomato Early Blight'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Mancozeb (Dithane M-45)'
  );

-- ==============================================================================
-- DISEASE: Tomato Late Blight
-- ==============================================================================
INSERT IGNORE INTO disease_info
    (disease_name, scientific_name, description, symptoms, causes, prevention_tips, severity_level)
VALUES (
    'Tomato Late Blight',
    'Phytophthora infestans',
    'Late Blight is a highly destructive disease capable of destroying entire tomato fields within days under favourable conditions. It was responsible for the Irish Potato Famine of the 1840s.',
    'Large, water-soaked, greasy-looking lesions on leaves that turn dark brown. White downy mould visible on underside of infected leaves in humid conditions. Fruit shows firm, brown rotting patches. Infected stems become dark and shrunken.',
    'Caused by the oomycete pathogen Phytophthora infestans. Spreads rapidly in cool (10–25°C), wet, and humid conditions through wind-borne spores. Can also spread through infected transplants.',
    'Plant resistant varieties where available. Avoid planting in poorly drained areas. Water at the base of plants, not overhead. Ensure good ventilation. Remove and destroy all infected plant material immediately — do not compost. Scout fields regularly during cool, wet periods.',
    'HIGH'
);

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Copper Hydroxide (Kocide)',
    'Mix 3–4g per litre of water. Apply every 5–7 days during high-risk periods (cool, wet weather). Ensure thorough coverage of all plant surfaces.',
    'Organic copper fungicides can slow disease spread but are less effective against established Late Blight than systemic chemical fungicides.',
    1
FROM disease_info WHERE disease_name = 'Tomato Late Blight'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Copper Hydroxide (Kocide)'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Metalaxyl + Mancozeb (Ridomil Gold)',
    'Apply as foliar spray at recommended label rate (typically 2–2.5g/litre). Begin applications preventively before disease appears. Repeat every 7–10 days.',
    'Metalaxyl is a systemic fungicide that moves within the plant. Resistance has been reported — rotate with other fungicide groups. Strictly observe PHI.',
    1
FROM disease_info WHERE disease_name = 'Tomato Late Blight'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Metalaxyl + Mancozeb (Ridomil Gold)'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Cymoxanil + Mancozeb (Curzate)',
    'Mix 2g per litre. Apply at onset of disease and repeat every 7 days. Use in rotation with other fungicide classes.',
    'Effective as a curative if applied within 3 days of infection. Wear full protective equipment during handling.',
    2
FROM disease_info WHERE disease_name = 'Tomato Late Blight'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Cymoxanil + Mancozeb (Curzate)'
  );

-- ==============================================================================
-- DISEASE: Tomato Leaf Mold
-- ==============================================================================
INSERT IGNORE INTO disease_info
    (disease_name, scientific_name, description, symptoms, causes, prevention_tips, severity_level)
VALUES (
    'Tomato Leaf Mold',
    'Passalora fulva',
    'Leaf Mold is a fungal disease primarily affecting tomato plants in humid, enclosed environments such as greenhouses. It reduces photosynthesis and, in severe cases, causes significant yield loss.',
    'Pale green to yellow patches appear on the upper surface of leaves. Corresponding olive-green to brown velvety mould growth appears on the undersides of leaves. Infected leaves eventually wither, curl, and drop. Fruit infection is less common.',
    'Caused by the fungus Passalora fulva (formerly Fulvia fulva). Favoured by high relative humidity (above 85%) and temperatures between 18–25°C. Spreads via airborne conidia. Common in protected cultivation.',
    'Improve greenhouse ventilation to reduce humidity below 85%. Use drip irrigation instead of overhead watering. Increase plant spacing for better airflow. Remove and destroy infected leaves early. Use resistant varieties.',
    'MED'
);

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Copper-based Spray',
    'Apply copper oxychloride or copper hydroxide at 3g per litre every 10 days when conditions favour disease development.',
    'Ensure good coverage of leaf undersides where mould grows.',
    1
FROM disease_info WHERE disease_name = 'Tomato Leaf Mold'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Copper-based Spray'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Chlorothalonil',
    'Apply as per label instructions every 7–10 days. Ensure thorough coverage including leaf undersides.',
    'Observe PHI stated on product label. Avoid applying in closed greenhouse without adequate personal protective equipment.',
    1
FROM disease_info WHERE disease_name = 'Tomato Leaf Mold'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Chlorothalonil'
  );

-- ==============================================================================
-- DISEASE: Tomato Septoria Leaf Spot
-- ==============================================================================
INSERT IGNORE INTO disease_info
    (disease_name, scientific_name, description, symptoms, causes, prevention_tips, severity_level)
VALUES (
    'Tomato Septoria Leaf Spot',
    'Septoria lycopersici',
    'Septoria Leaf Spot is one of the most destructive diseases of tomato foliage in humid regions. It can cause complete defoliation if not managed early.',
    'Small, water-soaked circular spots (2–4mm) with dark brown borders and grey-white centres on lower, older leaves. Tiny black fruiting bodies (pycnidia) visible in the centre of spots. Disease progresses upward through the plant.',
    'Caused by the fungus Septoria lycopersici. Spreads through water splash, insects, tools, and hands. Thrives in warm (20–25°C), wet, and humid conditions. Overwinters in infected plant debris and soil.',
    'Remove and destroy infected lower leaves as soon as symptoms appear. Avoid working in fields when plants are wet. Stake or cage plants to improve airflow. Use drip irrigation. Rotate crops for at least 2 years.',
    'MED'
);

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Copper Fungicide',
    'Mix 3g per litre and apply every 7–10 days beginning at first symptom. Cover all foliage thoroughly.',
    'Most effective when applied preventively. Remove heavily infected leaves before spraying.',
    1
FROM disease_info WHERE disease_name = 'Tomato Septoria Leaf Spot'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Copper Fungicide'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Mancozeb',
    'Apply at 2.5g per litre every 7–10 days. Begin applications at first sign of disease.',
    'Protective fungicide — works best before infection. Rotate with systemic fungicides for better control.',
    1
FROM disease_info WHERE disease_name = 'Tomato Septoria Leaf Spot'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Mancozeb'
  );

-- ==============================================================================
-- DISEASE: Tomato Spider Mites (Two-spotted spider mite)
-- ==============================================================================
INSERT IGNORE INTO disease_info
    (disease_name, scientific_name, description, symptoms, causes, prevention_tips, severity_level)
VALUES (
    'Tomato Spider Mites',
    'Tetranychus urticae',
    'Spider mites are tiny arachnid pests (not fungi or bacteria) that cause significant damage to tomato plants, especially in hot, dry conditions. They feed on plant cells, causing stippling, bronzing, and eventually leaf death.',
    'Fine stippling (tiny yellow or white dots) on upper leaf surfaces. Bronze or rusty discolouration of leaves. Fine silken webbing on undersides of leaves and between stems. Leaves eventually turn yellow, dry out, and drop. Mites visible with a 10x hand lens on leaf undersides.',
    'Caused by Tetranychus urticae (two-spotted spider mite). Populations explode in hot, dry, and dusty conditions. Broad-spectrum insecticide use kills natural predators, worsening outbreaks.',
    'Maintain plant health through regular watering — stressed plants are more susceptible. Avoid excessive nitrogen fertilization. Introduce or conserve natural predators (Phytoseiid predatory mites). Use reflective mulch. Dust plants and soil regularly with water in hot seasons.',
    'MED'
);

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Neem Oil (Azadirachtin)',
    'Mix 5ml neem oil per litre of water with a surfactant. Spray thoroughly on leaf undersides where mites concentrate. Apply every 5–7 days for 3 consecutive applications.',
    'Most effective against nymph stages. Does not kill eggs directly but disrupts mite lifecycle. Safe for most beneficial insects when applied correctly.',
    1
FROM disease_info WHERE disease_name = 'Tomato Spider Mites'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Neem Oil (Azadirachtin)'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Insecticidal Soap Spray',
    'Mix 5g of pure soap per litre of water. Apply directly to mite colonies on leaf undersides. Repeat every 5 days.',
    'Direct contact required for efficacy. Do not apply in full sun or when temperatures exceed 35°C — risk of phytotoxicity.',
    2
FROM disease_info WHERE disease_name = 'Tomato Spider Mites'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Insecticidal Soap Spray'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Abamectin (Vertimec)',
    'Apply at 1ml per litre of water. Spray undersides of leaves thoroughly. Repeat after 7 days if needed. Limit to 2 applications per season to prevent resistance.',
    'Abamectin is toxic to bees and aquatic organisms. Do not apply when crop is in flower. Observe a 3-day PHI. Wear full protective equipment.',
    1
FROM disease_info WHERE disease_name = 'Tomato Spider Mites'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Abamectin (Vertimec)'
  );

-- ==============================================================================
-- DISEASE: Tomato Target Spot
-- ==============================================================================
INSERT IGNORE INTO disease_info
    (disease_name, scientific_name, description, symptoms, causes, prevention_tips, severity_level)
VALUES (
    'Tomato Target Spot',
    'Corynespora cassiicola',
    'Target Spot is a fungal disease of increasing importance in tropical and subtropical tomato production. It can infect leaves, stems, and fruit.',
    'Round to irregular brown spots with concentric rings (target appearance) on leaves. Spots may have a yellow halo. Lesions can coalesce causing large dead areas. Dark elongated lesions on stems. Fruit shows sunken, dark, circular lesions.',
    'Caused by the fungus Corynespora cassiicola. Favoured by temperatures of 25–32°C and high humidity. Spreads through spores dispersed by wind and water. Common in tropical regions.',
    'Use resistant varieties where available. Ensure good plant spacing and airflow. Avoid overhead irrigation. Remove and destroy infected plant material. Apply fungicides preventively during high-risk weather.',
    'MED'
);

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Copper Oxychloride Spray',
    'Mix 3g per litre and apply every 10 days, covering all plant surfaces.',
    'Begin applications at first symptom or when conditions favour disease development.',
    1
FROM disease_info WHERE disease_name = 'Tomato Target Spot'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Copper Oxychloride Spray'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Azoxystrobin (Amistar)',
    'Apply at label recommended rate (typically 1ml/litre) every 10–14 days. Strobilurin class fungicide with protectant and curative activity.',
    'Do not use strobilurins exclusively — resistance risk is high. Rotate with multi-site fungicides such as mancozeb. Observe PHI on label.',
    1
FROM disease_info WHERE disease_name = 'Tomato Target Spot'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Azoxystrobin (Amistar)'
  );

-- ==============================================================================
-- DISEASE: Tomato Mosaic Virus
-- ==============================================================================
INSERT IGNORE INTO disease_info
    (disease_name, scientific_name, description, symptoms, causes, prevention_tips, severity_level)
VALUES (
    'Tomato Mosaic Virus',
    'Tomato mosaic virus (ToMV)',
    'Tomato Mosaic Virus is a highly contagious plant virus that can persist in soil and on tools for years. There is no cure once a plant is infected.',
    'Mottled light and dark green mosaic pattern on leaves. Leaves may be distorted, puckered, or fern-like (fernleaf). Yellowing between veins. Stunted growth. Fruit may show yellow blotches or internal browning.',
    'Caused by Tomato mosaic virus (ToMV), a tobamovirus. Spreads mechanically through hands, tools, and clothing. Can also spread through infected seeds. Very stable in the environment — survives on dead plant material for years.',
    'Use certified virus-free seeds or resistant varieties (Tm-2 gene). Wash hands thoroughly before handling plants. Disinfect pruning tools with 10% bleach or 70% alcohol between plants. Remove and destroy infected plants immediately. Control tobacco use near crops (ToMV is related to TMV). Do not touch plants after handling tobacco products.',
    'HIGH'
);

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Remove and Destroy Infected Plants',
    'At first signs of infection, remove the entire plant including roots. Place in sealed bags and dispose of — do not compost. Disinfect tools and hands immediately after.',
    'This is the primary management action. There is no chemical or biological cure for virus-infected plants.',
    1
FROM disease_info WHERE disease_name = 'Tomato Mosaic Virus'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Remove and Destroy Infected Plants'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Milk Spray (preventive)',
    'Mix 1 part fresh milk to 9 parts water. Spray on healthy plants as a preventive measure. Repeat weekly.',
    'Milk proteins have been shown to reduce mechanical transmission of tobamoviruses in research settings. Not a cure. Effectiveness varies.',
    2
FROM disease_info WHERE disease_name = 'Tomato Mosaic Virus'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Milk Spray (preventive)'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'No Chemical Cure Available',
    'There is no registered chemical treatment that cures virus-infected plants. Focus on prevention and roguing.',
    'Consult your local agricultural extension officer for guidance on resistant varieties available in your region.',
    1
FROM disease_info WHERE disease_name = 'Tomato Mosaic Virus'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'No Chemical Cure Available'
  );

-- ==============================================================================
-- DISEASE: Tomato Yellow Leaf Curl Virus
-- ==============================================================================
INSERT IGNORE INTO disease_info
    (disease_name, scientific_name, description, symptoms, causes, prevention_tips, severity_level)
VALUES (
    'Tomato Yellow Leaf Curl Virus',
    'Tomato yellow leaf curl virus (TYLCV)',
    'Tomato Yellow Leaf Curl Virus (TYLCV) is one of the most devastating tomato diseases globally. It is transmitted exclusively by the whitefly Bemisia tabaci and can cause up to 100% crop loss in severe outbreaks.',
    'Upward curling and cupping of young leaves. Yellowing (chlorosis) of leaf margins. Stunted plant growth. Flowers may drop. Fruit set is severely reduced on infected plants. Infected plants appear bushy due to shortened internodes.',
    'Caused by Tomato yellow leaf curl virus (TYLCV), a begomovirus. Transmitted in a persistent manner by the silverleaf whitefly (Bemisia tabaci). Not transmitted through seeds, soil, or mechanical contact.',
    'Plant resistant or tolerant varieties (Ty genes). Use whitefly-proof insect-proof nets in nurseries. Control whitefly populations with yellow sticky traps and insecticides. Remove and destroy infected plants. Avoid growing tomatoes adjacent to other whitefly host crops.',
    'HIGH'
);

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Neem Oil (Whitefly Control)',
    'Mix 5ml neem oil per litre with surfactant. Spray undersides of leaves where whiteflies congregate. Apply every 5–7 days.',
    'Targets the whitefly vector, not the virus itself. Most effective in early stages of whitefly establishment.',
    1
FROM disease_info WHERE disease_name = 'Tomato Yellow Leaf Curl Virus'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Neem Oil (Whitefly Control)'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Yellow Sticky Traps',
    'Place yellow sticky traps (25 per hectare) at canopy level throughout the crop. Monitor weekly and replace when full.',
    'Effective for monitoring whitefly population levels. Reduces adult populations but not a standalone control method.',
    2
FROM disease_info WHERE disease_name = 'Tomato Yellow Leaf Curl Virus'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Yellow Sticky Traps'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Imidacloprid (Confidor) — Soil Drench',
    'Apply as soil drench at transplanting: 0.5ml per litre per plant. Provides systemic whitefly control for 4–6 weeks.',
    'Neonicotinoid — observe restrictions on use near flowering crops and pollinators. Do not apply once plants are flowering. Follow label PHI. Resistance may develop with repeated use.',
    1
FROM disease_info WHERE disease_name = 'Tomato Yellow Leaf Curl Virus'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Imidacloprid (Confidor) — Soil Drench'
  );

-- ==============================================================================
-- DISEASE: Tomato Bacterial Spot
-- ==============================================================================
INSERT IGNORE INTO disease_info
    (disease_name, scientific_name, description, symptoms, causes, prevention_tips, severity_level)
VALUES (
    'Tomato Bacterial Spot',
    'Xanthomonas vesicatoria',
    'Bacterial Spot is a common and economically important disease that affects both tomato leaves and fruit, reducing marketable yield and plant vigour.',
    'Small (2–3mm), water-soaked spots on leaves that become angular and dark brown with a yellow halo. Spots may develop a greasy appearance. Severe infection causes yellowing and defoliation. Fruit shows raised, scabby lesions that are first water-soaked then turn dark brown and corky.',
    'Caused by Xanthomonas species (primarily X. vesicatoria). Spreads through infected seed, transplants, rain splash, and wind-driven rain. Thrives in warm (24–30°C), wet, and humid conditions.',
    'Use certified disease-free seeds. Hot-water seed treatment (50°C for 25 minutes) before planting. Avoid overhead irrigation. Use copper bactericides preventively during wet periods. Remove and destroy infected plant debris. Rotate crops for 2–3 years.',
    'MED'
);

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Copper Hydroxide Spray',
    'Mix 3g per litre and apply every 7 days during wet weather. Begin applications before disease appears or at first symptoms.',
    'Copper sprays are bacteriostatic (slow growth) but not bactericidal (do not kill). Copper resistance has been reported in some Xanthomonas strains.',
    1
FROM disease_info WHERE disease_name = 'Tomato Bacterial Spot'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Copper Hydroxide Spray'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Copper + Mancozeb (tank mix)',
    'Mix copper fungicide with mancozeb at respective label rates. The combination improves efficacy. Apply every 7–10 days during high-risk conditions.',
    'Adding mancozeb to copper can improve bacterial control. Observe all PHI and safety requirements for both products.',
    1
FROM disease_info WHERE disease_name = 'Tomato Bacterial Spot'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Copper + Mancozeb (tank mix)'
  );

-- ==============================================================================
-- DISEASE: Tomato Healthy
-- ==============================================================================
INSERT IGNORE INTO disease_info
    (disease_name, scientific_name, description, symptoms, causes, prevention_tips, severity_level)
VALUES (
    'Tomato Healthy',
    NULL,
    'The tomato leaf appears healthy with no visible signs of disease, pest damage, or nutrient deficiency.',
    'Leaves are uniformly green, firm, and without spots, lesions, mould, discolouration, or distortion.',
    'N/A — no disease detected.',
    'Continue current good agricultural practices: proper irrigation, balanced fertilization, regular scouting, crop rotation, and sanitation.',
    'LOW'
);

-- ==============================================================================
-- DISEASE: Nutrient Deficiency
-- ==============================================================================
INSERT IGNORE INTO disease_info
    (disease_name, scientific_name, description, symptoms, causes, prevention_tips, severity_level)
VALUES (
    'Nutrient Deficiency',
    'Abiotic / Nutritional Disorder',
    'Nutrient deficiency in tomatoes occurs when plants receive insufficient essential macronutrients (nitrogen, phosphorus, potassium, calcium, magnesium, sulfur) or micronutrients (iron, zinc, boron, manganese). Deficiencies impair physiological processes, weaken plant vigor, stunt vegetative growth, reduce fruit yield, and increase susceptibility to secondary diseases.',
    'Symptoms depend on the specific deficient nutrient: overall yellowing (chlorosis) of older lower leaves typically indicates nitrogen deficiency; purplish coloration along stems and underside of leaves suggests phosphorus shortage; leaf margin browning or scorch denotes potassium deficiency; blossom-end rot on developing fruit indicates calcium deficiency; and interveinal yellowing with distinct green veins highlights magnesium or iron deficiency.',
    'Imbalanced soil pH hindering root absorption, prolonged nutrient depletion from continuous cropping without replenishment, inadequate soil organic matter, compacted or waterlogged soils causing root suffocation, or excessive leaching in coarse sandy soils.',
    'Perform regular soil testing and foliar tissue analysis to establish accurate nutritional baselines. Maintain soil pH between 6.0 and 6.8 for optimal nutrient availability. Incorporate well-cured compost or cover crops prior to planting. Practice consistent, monitored irrigation to maintain calcium transport, and avoid unguided single-nutrient applications.',
    'LOW'
);

-- Nutrient Deficiency Remedies
INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Compost and Organic Soil Conditioning',
    'Incorporate well-rotted organic compost, aged farmyard manure, or vermicompost as a surface top-dressing around the plant drip line. Specific soil amendments and nutrient volumes should be determined through comprehensive laboratory soil testing to restore nutritional balance without over-fertilization.',
    'Organic amendments enhance cation exchange capacity (CEC), stimulate beneficial soil microbes, improve moisture retention, and supply a gentle, slow-release matrix of macro- and micronutrients.',
    1
FROM disease_info WHERE disease_name = 'Nutrient Deficiency'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Compost and Organic Soil Conditioning'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'ORGANIC', 'Foliar Organic Seaweed and Micronutrient Spray',
    'Apply liquid seaweed extract or aerated compost tea as a fine foliar mist during early morning or late afternoon. Conduct a foliar tissue test to identify the specific nutrient shortage before application to ensure appropriate micronutrient delivery.',
    'Foliar feeding allows rapid nutrient absorption across leaf stomata and cuticles to alleviate temporary micronutrient stress while root-zone soil conditions are corrected.',
    2
FROM disease_info WHERE disease_name = 'Nutrient Deficiency'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Foliar Organic Seaweed and Micronutrient Spray'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Targeted Soil Amendment Based on Soil Test',
    'Conduct professional soil testing to accurately identify the specific deficient nutrient and evaluate soil pH before applying fertilizers. Apply targeted soil amendments or fertilizer formulations strictly in accordance with laboratory soil test recommendations. Do not apply arbitrary dosages; over-application can cause root burn, excessive salinity, and nutrient antagonism.',
    'Soil pH directly regulates nutrient bioavailability. For example, iron and phosphorus become unavailable in alkaline soils (pH > 7.0), while calcium and magnesium uptake is hindered in acidic soils (pH < 6.0). Adjusting soil pH is essential for fertilizer efficiency.',
    1
FROM disease_info WHERE disease_name = 'Nutrient Deficiency'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Targeted Soil Amendment Based on Soil Test'
  );

INSERT IGNORE INTO disease_remedies (disease_id, remedy_type, remedy_name, application, notes, display_order)
SELECT id, 'CHEMICAL', 'Balanced Water-Soluble Plant Nutrition',
    'When soil or tissue analysis confirms an active macro- or micronutrient deficiency during rapid vegetative growth or fruit development, apply a balanced water-soluble fertilizer formulation suited to the crop stage. All application rates must follow soil test recommendations and regional agricultural extension guidance rather than generic dosing.',
    'Water-soluble fertilizers provide immediate nutrient availability. Always adhere to recommended safety intervals and prevent chemical runoff into nearby water bodies.',
    2
FROM disease_info WHERE disease_name = 'Nutrient Deficiency'
  AND NOT EXISTS (
      SELECT 1 FROM disease_remedies
      WHERE disease_remedies.disease_id = disease_info.id
        AND disease_remedies.remedy_name = 'Balanced Water-Soluble Plant Nutrition'
  );

-- ==============================================================================
-- Verification (uncomment to check row counts after running)
-- ==============================================================================
-- SELECT disease_name, severity_level FROM disease_info ORDER BY disease_name;
-- SELECT d.disease_name, r.remedy_type, r.remedy_name FROM disease_remedies r JOIN disease_info d ON r.disease_id = d.id ORDER BY d.disease_name, r.remedy_type;
