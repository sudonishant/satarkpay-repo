# SatarkPay demo video — Hindi narration script (≈ 6:30–7:00)

Har section ka beat + approximate duration. Audio `assets/demo/voice/` me
generate hota hai, slides `tools/make_demo_video.py` se.

## 1 · INTRO (≈ 0:00–0:55)
Namaskar! Main Nishant Kumar, Team SCAMURAI ki taraf se — SANGYAN 2026 ke liye,
SEBI, NSDL aur IIT-BHU ke is hackathon mein aapka swagat hai. Aaj main aapko
dikhata hoon SatarkPay, yaani Satarakpae. Ek aisa protection layer jo UPI fraud
ko rokta hai paisa jaane se pehle — chahe wo web app ho ya android mobile app.
Do second ke liye sochiye — aaj tak har fraud tool paisa jaane ke baad react
karta hai. SatarkPay ka poora idea ulta hai. Ye 60 second pehle kaam karta hai.
Chaliye dekhte hain ye kaise kaam karta hai.

## 2 · PROBLEM (≈ 0:55–1:50)
India mein UPI fraud koi hacking nahi hai — ye ek conversation hai. Koi aapko
bank officer banke call karta hai, koi CBI ya police banke digital arrest ka
drama karta hai, aur koi kehta hai — is QR code ko scan kijiye, aapko refund
milega. Lekin asliyat ye hai ki QR scan sirf paisa kaatta hai — paisa kabhi
jamaa nahi karta. Aap screen par jo asli fraud screenshots dekh rahe hain —
digital arrest, fake KYC APK, refund QR, fake job — ye sab hamari scam library
se hain. In sab mein teen cheezein common hain — jaldi, darr, aur ek galat
disha mein paisa bhejne ka pressure. Aur isi 60 second ke window mein paisa
hamesha chala jaata hai. Sawal ye hai — usi window mein koi cheez aapko rok
kyun nahi sakti? SatarkPay wahi karta hai.

## 3 · WEB APP (≈ 1:50–4:10)
Ab dekhte hain SatarkPay web app. Browser mein khulti hai — na install, na
account, na server. Kuch bhi upload nahi hota. Dashboard se shuru karte hain.
Sabse powerful tool hai — Check a payment. Aapko sirf 9 sawal ke jawab dene
hain — jaise, kya aap call par hain? Kya pehli baar is payee ko paisa bhej
rahe hain? Kya amount aapke normal se bahut zyada hai? Kya screen sharing chal
rahi hai? Har signal ka ek chhota number hota hai — plus chaar, plus teen, plus
do — aur sab judkar ek score banta hai. Dekhiye, koi black-box AI nahi — poori
arithmetic khuli hai, aap khud jod sakte hain. Score ke hisaab se chaar honest
tiers — LOW, CAUTION, HIGH, aur CRITICAL. LOW mein aap aaram se bhej sakte
hain. CAUTION par 30 second ka cooling time. HIGH par 60 second. Aur jaise hi
score critical hota hai — pay button hi ghayab ho jaata hai. Aur ek detail
bahut zaroori hai — countdown nahi dikhaaya jaata, kyunki payment kabhi unlock
hone wali nahi hoti. Ab Check a message page. Koi bhi WhatsApp ya SMS paste
kijiye — fraud ke 13 patterns turant match ho jaate hain, saath mein 4
legitimacy markers, taaki asli bank message par false alarm na aaye. Aur sabse
zaroori — aapke account number, UPI ID, OTP sab mask ho jaate hain, screen par
bhi aur copy karte waqt bhi. Phir hai link checker. Hum blacklist use nahi
karte — blacklist to banate hi purani ho jaati hai. Link ko toda jaata hai —
brand, action word, digits, TLD — aur asli domain se compare kiya jaata hai.
Aur scam library — 10 asli fraud patterns, screenshots ke saath. Har pattern ke
saath wo physical fact likha hai jo us dhaanche ko namumkin banata hai.

## 4 · SUPPORT (≈ 4:10–5:10)
Lekin agar paisa phir bhi chala gaya to? Panic nahi. SatarkPay ke paas 10-minute
emergency mode hai. Saat steps — bilkul us order mein jo result badalta hai.
Sabse pehle, 1930 par call. Ye golden hour hai. Phir cybercrime dot gov dot in
par financial fraud report. Bank ko call karke card, UPI aur net banking freeze
karaaiye. Evidence bachaiye — transaction ID, UTR number, screenshots, chats.
Aur dekhiye — complaint packet generator ek click mein poora structured record
bana deta hai — date, time, masked UPI ID, amount, sab kuch — jo aap seedha
bank aur cybercrime portal ko de sakte hain. Ek cheez aur — recovery agent ke
naam se agla call aane wala hai, jo aur paise maangega. Usko ek rupya bhi mat
dijiye. Aur mere sabse favourite feature — Hindi voice aur senior mode. Bade
buzurgon ke liye app har step awaaz mein samjhaati hai — bina padhe, bina
ghabraye.

## 5 · ANDROID (≈ 5:10–6:30)
Aur ab mobile app — poori native, Kotlin aur Jetpack Compose mein bani. 14
screens, full stack, aur same premium design. Ghar ke dashboard se shuru
karte hain. Screenshot Radar — jo WhatsApp ke suspicious screenshots detect
karta hai, aur agar 30 minute mein bahut saare payment screenshots aa rahe
hain, to burst warning deta hai. Phir Sanchalak AI chat — Hindi voice ke saath
— jo har sawal ka jawab simple bhasha mein deta hai, chahe wo job offer ho ya
koi loan link. Wallet audit — jo aapke silent AutoPay mandates dikhata hai jo
mahine ka paisa kaat-te rehte hain. Intel feed — scam ki nayi khabrein. Aur
app security scanner — jo aapke phone mein chhupe remote-access apps dhundhta
hai. Emergency mode, grievance ladder, evidence pack — sab mobile par bhi
available hain. Aur sabse important baat — rule engine bilkul offline chalta
hai. Internet na ho, tab bhi protection poori tarah chalu rahega. UPI PIN,
OTP, CVV — ye app kabhi nahi maangti. Ye hi SatarkPay ka rule hai.

## 6 · RESULTS + OUTRO (≈ 6:30–7:10)
Jaldi numbers bhi dekh lijiye — message classifier ka F1 score 92.7 percent,
engine ki speed 12 millisecond se bhi kam, 139 tests pass, aur 8 mein se 8
pages poore offline kaam karte hain. Har number is repository mein verify kiya
ja sakta hai. SatarkPay ka ek hi maksad hai — paisa bhejne se pehle 60 second,
aur fraud ke baad 60 minute ka sahi support. Aap hi nahi, aapka poora parivaar
surakshit. Aaj hi try kijiye — link description mein hai. Dhanyavaad! Team
SCAMURAI ki taraf se — SatarkPay. Jai Hind.
