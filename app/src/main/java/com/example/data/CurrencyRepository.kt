package com.example.data

import com.example.model.CurrencyInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyRepository {

    val ALL_CURRENCIES: List<CurrencyInfo> = listOf(
        CurrencyInfo("INR", "Indian Rupee", "₹", "🇮🇳", "India", priority = 1),
        CurrencyInfo("USD", "US Dollar", "$", "🇺🇸", "United States", priority = 2),
        CurrencyInfo("EUR", "Euro", "€", "🇪🇺", "European Union", priority = 3),
        CurrencyInfo("GBP", "British Pound", "£", "🇬🇧", "United Kingdom", priority = 4),
        CurrencyInfo("AED", "UAE Dirham", "د.إ", "🇦🇪", "United Arab Emirates", priority = 5),
        CurrencyInfo("CAD", "Canadian Dollar", "C$", "🇨🇦", "Canada", priority = 6),
        CurrencyInfo("AUD", "Australian Dollar", "A$", "🇦🇺", "Australia", priority = 7),
        CurrencyInfo("SAR", "Saudi Riyal", "﷼", "🇸🇦", "Saudi Arabia", priority = 8),
        CurrencyInfo("KWD", "Kuwaiti Dinar", "د.ك", "🇰🇼", "Kuwait", priority = 9),
        CurrencyInfo("QAR", "Qatari Riyal", "﷼", "🇶🇦", "Qatar", priority = 10),
        CurrencyInfo("SGD", "Singapore Dollar", "S$", "🇸🇬", "Singapore", priority = 11),
        CurrencyInfo("JPY", "Japanese Yen", "¥", "🇯🇵", "Japan", priority = 12),
        CurrencyInfo("CNY", "Chinese Yuan", "¥", "🇨🇳", "China", priority = 13),
        CurrencyInfo("CHF", "Swiss Franc", "CHF", "🇨🇭", "Switzerland", priority = 14),
        CurrencyInfo("OMR", "Omani Rial", "﷼", "🇴🇲", "Oman", priority = 15),
        CurrencyInfo("BHD", "Bahraini Dinar", ".د.ب", "🇧🇭", "Bahrain", priority = 16),
        CurrencyInfo("MYR", "Malaysian Ringgit", "RM", "🇲🇾", "Malaysia", priority = 17),
        CurrencyInfo("THB", "Thai Baht", "฿", "🇹🇭", "Thailand", priority = 18),
        CurrencyInfo("NZD", "New Zealand Dollar", "NZ$", "🇳🇿", "New Zealand", priority = 19),
        CurrencyInfo("HKD", "Hong Kong Dollar", "HK$", "🇭🇰", "Hong Kong", priority = 20),
        CurrencyInfo("KRW", "South Korean Won", "₩", "🇰🇷", "South Korea", priority = 21),
        CurrencyInfo("ZAR", "South African Rand", "R", "🇿🇦", "South Africa", priority = 22),
        CurrencyInfo("BRL", "Brazilian Real", "R$", "🇧🇷", "Brazil", priority = 23),
        CurrencyInfo("RUB", "Russian Ruble", "₽", "🇷🇺", "Russia", priority = 24),
        CurrencyInfo("TRY", "Turkish Lira", "₺", "🇹🇷", "Turkey", priority = 25),
        CurrencyInfo("IDR", "Indonesian Rupiah", "Rp", "🇮🇩", "Indonesia", priority = 26),
        CurrencyInfo("MXN", "Mexican Peso", "Mex$", "🇲🇽", "Mexico", priority = 27),
        CurrencyInfo("PHP", "Philippine Peso", "₱", "🇵🇭", "Philippines", priority = 28),
        CurrencyInfo("VND", "Vietnamese Dong", "₫", "🇻🇳", "Vietnam", priority = 29),
        CurrencyInfo("PKR", "Pakistani Rupee", "₨", "🇵🇰", "Pakistan", priority = 30),
        CurrencyInfo("BDT", "Bangladeshi Taka", "৳", "🇧🇩", "Bangladesh", priority = 31),
        CurrencyInfo("LKR", "Sri Lankan Rupee", "Rs", "🇱🇰", "Sri Lanka", priority = 32),
        CurrencyInfo("NPR", "Nepalese Rupee", "रू", "🇳🇵", "Nepal", priority = 33),
        CurrencyInfo("SEK", "Swedish Krona", "kr", "🇸🇪", "Sweden", priority = 34),
        CurrencyInfo("NOK", "Norwegian Krone", "kr", "🇳🇴", "Norway", priority = 35),
        CurrencyInfo("DKK", "Danish Krone", "kr", "🇩🇰", "Denmark", priority = 36),
        CurrencyInfo("PLN", "Polish Zloty", "zł", "🇵🇱", "Poland", priority = 37),
        CurrencyInfo("ILS", "Israeli Shekel", "₪", "🇮🇱", "Israel", priority = 38),
        CurrencyInfo("EGP", "Egyptian Pound", "E£", "🇪🇬", "Egypt", priority = 39),
        CurrencyInfo("NGN", "Nigerian Naira", "₦", "🇳🇬", "Nigeria", priority = 40),
        CurrencyInfo("KES", "Kenyan Shilling", "KSh", "🇰🇪", "Kenya", priority = 41),
        CurrencyInfo("GHS", "Ghanaian Cedi", "GH₵", "🇬🇭", "Ghana", priority = 42),
        CurrencyInfo("CLP", "Chilean Peso", "CLP$", "🇨🇱", "Chile", priority = 43),
        CurrencyInfo("COP", "Colombian Peso", "COL$", "🇨🇴", "Colombia", priority = 44),
        CurrencyInfo("PEN", "Peruvian Sol", "S/", "🇵🇪", "Peru", priority = 45),
        CurrencyInfo("ARS", "Argentine Peso", "$", "🇦🇷", "Argentina", priority = 46),
        CurrencyInfo("CZK", "Czech Koruna", "Kč", "🇨🇿", "Czech Republic", priority = 47),
        CurrencyInfo("HUF", "Hungarian Forint", "Ft", "🇭🇺", "Hungary", priority = 48),
        CurrencyInfo("RON", "Romanian Leu", "lei", "🇷🇴", "Romania", priority = 49),
        CurrencyInfo("BGN", "Bulgarian Lev", "лв", "🇧🇬", "Bulgaria", priority = 50),
        CurrencyInfo("TWD", "New Taiwan Dollar", "NT$", "🇹🇼", "Taiwan", priority = 51),
        CurrencyInfo("MAD", "Moroccan Dirham", "DH", "🇲🇦", "Morocco", priority = 52),
        CurrencyInfo("JOD", "Jordanian Dinar", "JD", "🇯🇴", "Jordan", priority = 53),
        CurrencyInfo("BND", "Brunei Dollar", "B$", "🇧🇳", "Brunei", priority = 54),
        CurrencyInfo("FJD", "Fijian Dollar", "FJ$", "🇫🇯", "Fiji", priority = 55),
        CurrencyInfo("MUR", "Mauritian Rupee", "₨", "🇲🇺", "Mauritius", priority = 56),
        CurrencyInfo("ISK", "Icelandic Krona", "kr", "🇮🇸", "Iceland", priority = 57),
        CurrencyInfo("HRK", "Croatian Kuna", "kn", "🇭🇷", "Croatia", priority = 58),
        CurrencyInfo("RSD", "Serbian Dinar", "дин.", "🇷🇸", "Serbia", priority = 59),
        CurrencyInfo("UAH", "Ukrainian Hryvnia", "₴", "🇺🇦", "Ukraine", priority = 60),
        CurrencyInfo("KZT", "Kazakhstani Tenge", "₸", "🇰🇿", "Kazakhstan", priority = 61),
        CurrencyInfo("UZS", "Uzbekistani Som", "so'm", "🇺🇿", "Uzbekistan", priority = 62),
        CurrencyInfo("AZN", "Azerbaijani Manat", "₼", "🇦🇿", "Azerbaijan", priority = 63),
        CurrencyInfo("GEL", "Georgian Lari", "₾", "🇬🇪", "Georgia", priority = 64),
        CurrencyInfo("AMD", "Armenian Dram", "֏", "🇦🇲", "Armenia", priority = 65),
        CurrencyInfo("IQD", "Iraqi Dinar", "ع.د", "🇮🇶", "Iraq", priority = 66),
        CurrencyInfo("LBP", "Lebanese Pound", "L£", "🇱🇧", "Lebanon", priority = 67),
        CurrencyInfo("TZS", "Tanzanian Shilling", "TSh", "🇹🇿", "Tanzania", priority = 68),
        CurrencyInfo("UGX", "Ugandan Shilling", "USh", "🇺🇬", "Uganda", priority = 69),
        CurrencyInfo("ETB", "Ethiopian Birr", "Br", "🇪🇹", "Ethiopia", priority = 70),
        CurrencyInfo("DZD", "Algerian Dinar", "دج", "🇩🇿", "Algeria", priority = 71),
        CurrencyInfo("TND", "Tunisian Dinar", "DT", "🇹🇳", "Tunisia", priority = 72),
        CurrencyInfo("CRC", "Costa Rican Colon", "₡", "🇨🇷", "Costa Rica", priority = 73),
        CurrencyInfo("DOP", "Dominican Peso", "RD$", "🇩🇴", "Dominican Republic", priority = 74),
        CurrencyInfo("UYU", "Uruguayan Peso", "\$U", "🇺🇾", "Uruguay", priority = 75),
        CurrencyInfo("BOB", "Bolivian Boliviano", "Bs", "🇧🇴", "Bolivia", priority = 76),
        CurrencyInfo("PYG", "Paraguayan Guarani", "₲", "🇵🇾", "Paraguay", priority = 77),
        CurrencyInfo("BAM", "Bosnia Convertible Mark", "KM", "🇧🇦", "Bosnia", priority = 78),
        CurrencyInfo("ALL", "Albanian Lek", "L", "🇦🇱", "Albania", priority = 79),
        CurrencyInfo("MKD", "Macedonian Denar", "ден", "🇲🇰", "North Macedonia", priority = 80),
        CurrencyInfo("MOP", "Macanese Pataca", "MOP$", "🇲🇴", "Macau", priority = 81),
        CurrencyInfo("MNT", "Mongolian Tugrik", "₮", "🇲🇳", "Mongolia", priority = 82),
        CurrencyInfo("LAK", "Laotian Kip", "₭", "🇱🇦", "Laos", priority = 83),
        CurrencyInfo("KHR", "Cambodian Riel", "៛", "🇰🇭", "Cambodia", priority = 84),
        CurrencyInfo("MMK", "Myanmar Kyat", "K", "🇲🇲", "Myanmar", priority = 85),
        CurrencyInfo("BWP", "Botswanan Pula", "P", "🇧🇼", "Botswana", priority = 86),
        CurrencyInfo("NAD", "Namibian Dollar", "N$", "🇳🇦", "Namibia", priority = 87),
        CurrencyInfo("ZMW", "Zambian Kwacha", "ZK", "🇿🇲", "Zambia", priority = 88),
        CurrencyInfo("MZN", "Mozambican Metical", "MT", "🇲🇿", "Mozambique", priority = 89),
        CurrencyInfo("AOA", "Angolan Kwanza", "Kz", "🇦🇴", "Angola", priority = 90),
        CurrencyInfo("JMD", "Jamaican Dollar", "J$", "🇯🇲", "Jamaica", priority = 91),
        CurrencyInfo("TTD", "Trinidad & Tobago Dollar", "TT$", "🇹🇹", "Trinidad", priority = 92),
        CurrencyInfo("XCD", "East Caribbean Dollar", "EC$", "🇦🇬", "Caribbean", priority = 93),
        CurrencyInfo("BBD", "Barbadian Dollar", "Bds$", "🇧🇧", "Barbados", priority = 94),
        CurrencyInfo("BSD", "Bahamian Dollar", "B$", "🇧🇸", "Bahamas", priority = 95),
        CurrencyInfo("BMD", "Bermudan Dollar", "BD$", "🇧🇲", "Bermuda", priority = 96),
        CurrencyInfo("KYD", "Cayman Islands Dollar", "CI$", "🇰🇾", "Cayman Islands", priority = 97),
        CurrencyInfo("SCR", "Seychellois Rupee", "SR", "🇸🇨", "Seychelles", priority = 98),
        CurrencyInfo("MVR", "Maldivian Rufiyaa", "Rf", "🇲🇻", "Maldives", priority = 99),
        CurrencyInfo("BIF", "Burundian Franc", "FBu", "🇧🇮", "Burundi", priority = 100),
        CurrencyInfo("RWF", "Rwandan Franc", "FRw", "🇷🇼", "Rwanda", priority = 101),
        CurrencyInfo("MWK", "Malawian Kwacha", "MK", "🇲🇼", "Malawi", priority = 102),
        CurrencyInfo("MDL", "Moldovan Leu", "L", "🇲🇩", "Moldova", priority = 103),
        CurrencyInfo("HTG", "Haitian Gourde", "G", "🇭🇹", "Haiti", priority = 104),
        CurrencyInfo("GTQ", "Guatemalan Quetzal", "Q", "🇬🇹", "Guatemala", priority = 105),
        CurrencyInfo("HNL", "Honduran Lempira", "L", "🇭🇳", "Honduras", priority = 106),
        CurrencyInfo("NIO", "Nicaraguan Cordoba", "C$", "🇳🇮", "Nicaragua", priority = 107),
        CurrencyInfo("PAB", "Panamanian Balboa", "B/.", "🇵🇦", "Panama", priority = 108),
        CurrencyInfo("YER", "Yemeni Rial", "﷼", "🇾🇪", "Yemen", priority = 109),
        CurrencyInfo("SYP", "Syrian Pound", "LS", "🇸🇾", "Syria", priority = 110),
        CurrencyInfo("AFN", "Afghan Afghani", "؋", "🇦🇫", "Afghanistan", priority = 111),
        CurrencyInfo("TJS", "Tajikistani Somoni", "SM", "🇹🇯", "Tajikistan", priority = 112),
        CurrencyInfo("KGS", "Kyrgystani Som", "с", "🇰🇬", "Kyrgyzstan", priority = 113),
        CurrencyInfo("TMT", "Turkmenistani Manat", "T", "🇹🇲", "Turkmenistan", priority = 114),
        CurrencyInfo("SOS", "Somali Shilling", "Sh", "🇸🇴", "Somalia", priority = 115),
        CurrencyInfo("DJF", "Djiboutian Franc", "Fdj", "🇩🇯", "Djibouti", priority = 116),
        CurrencyInfo("CDF", "Congolese Franc", "FC", "🇨🇩", "DR Congo", priority = 117),
        CurrencyInfo("GNF", "Guinean Franc", "FG", "🇬🇳", "Guinea", priority = 118),
        CurrencyInfo("XOF", "West African CFA Franc", "CFA", "🇨🇮", "West Africa", priority = 119),
        CurrencyInfo("XAF", "Central African CFA Franc", "FCFA", "🇨🇲", "Central Africa", priority = 120),
        CurrencyInfo("XPF", "CFP Franc", "₣", "🇵🇫", "French Polynesia", priority = 121),
        CurrencyInfo("WST", "Samoan Tala", "WS$", "🇼🇸", "Samoa", priority = 122),
        CurrencyInfo("TOP", "Tongan Pa'anga", "T$", "🇹🇴", "Tonga", priority = 123),
        CurrencyInfo("VUV", "Vanuatu Vatu", "VT", "🇻🇺", "Vanuatu", priority = 124),
        CurrencyInfo("SBD", "Solomon Islands Dollar", "SI$", "🇸🇧", "Solomon Islands", priority = 125),
        CurrencyInfo("PGK", "Papua New Guinean Kina", "K", "🇵🇬", "Papua New Guinea", priority = 126),
        CurrencyInfo("CVE", "Cape Verdean Escudo", "Esc", "🇨🇻", "Cape Verde", priority = 127),
        CurrencyInfo("STN", "Sao Tome Dobra", "Db", "🇸🇹", "Sao Tome", priority = 128),
        CurrencyInfo("SZL", "Swazi Lilangeni", "E", "🇸🇿", "Eswatini", priority = 129),
        CurrencyInfo("LSL", "Lesotho Loti", "L", "🇱🇸", "Lesotho", priority = 130),
        CurrencyInfo("GYD", "Guyanese Dollar", "G$", "🇬🇾", "Guyana", priority = 131),
        CurrencyInfo("SRD", "Surinamese Dollar", "Sr$", "🇸🇷", "Suriname", priority = 132),
        CurrencyInfo("BZD", "Belize Dollar", "BZ$", "🇧🇿", "Belize", priority = 133),
        CurrencyInfo("GIP", "Gibraltar Pound", "£", "🇬🇮", "Gibraltar", priority = 134),
        CurrencyInfo("FKP", "Falkland Islands Pound", "£", "🇫🇰", "Falkland Islands", priority = 135),
        CurrencyInfo("SHP", "Saint Helena Pound", "£", "🇸🇭", "Saint Helena", priority = 136),
        // Additional Sovereign Countries to Complete 195+ Countries Globally
        CurrencyInfo("ALL", "Albanian Lek", "L", "🇦🇱", "Albania", priority = 137),
        CurrencyInfo("ANG", "NL Antillean Guilder", "ƒ", "🇨🇼", "Curacao & Sint Maarten", priority = 138),
        CurrencyInfo("AOA", "Angolan Kwanza", "Kz", "🇦🇴", "Angola", priority = 139),
        CurrencyInfo("AWG", "Aruban Florin", "ƒ", "🇦🇼", "Aruba", priority = 140),
        CurrencyInfo("BAM", "Bosnia Convertible Mark", "KM", "🇧🇦", "Bosnia & Herzegovina", priority = 141),
        CurrencyInfo("BTN", "Bhutanese Ngultrum", "Nu.", "🇧🇹", "Bhutan", priority = 142),
        CurrencyInfo("BYN", "Belarusian Ruble", "Br", "🇧🇾", "Belarus", priority = 143),
        CurrencyInfo("CUP", "Cuban Peso", "$", "🇨🇺", "Cuba", priority = 144),
        CurrencyInfo("ERN", "Eritrean Nakfa", "Nfk", "🇪🇷", "Eritrea", priority = 145),
        CurrencyInfo("GMD", "Gambian Dalasi", "D", "🇬🇲", "Gambia", priority = 146),
        CurrencyInfo("IRR", "Iranian Rial", "﷼", "🇮🇷", "Iran", priority = 147),
        CurrencyInfo("KMF", "Comorian Franc", "CF", "🇰🇲", "Comoros", priority = 148),
        CurrencyInfo("KPW", "North Korean Won", "₩", "🇰🇵", "North Korea", priority = 149),
        CurrencyInfo("LRD", "Liberian Dollar", "L$", "🇱🇷", "Liberia", priority = 150),
        CurrencyInfo("LYD", "Libyan Dinar", "LD", "🇱🇾", "Libya", priority = 151),
        CurrencyInfo("MGA", "Malagasy Ariary", "Ar", "🇲🇬", "Madagascar", priority = 152),
        CurrencyInfo("MKD", "Macedonian Denar", "den", "🇲🇰", "North Macedonia", priority = 153),
        CurrencyInfo("MRU", "Mauritanian Ouguiya", "UM", "🇲🇷", "Mauritania", priority = 154),
        CurrencyInfo("SDG", "Sudanese Pound", "SDG", "🇸🇩", "Sudan", priority = 155),
        CurrencyInfo("SLE", "Sierra Leonean Leone", "Le", "🇸🇱", "Sierra Leone", priority = 156),
        CurrencyInfo("SSP", "South Sudanese Pound", "SSP", "🇸🇸", "South Sudan", priority = 157),
        CurrencyInfo("TVD", "Tuvaluan Dollar", "$", "🇹🇻", "Tuvalu", priority = 158),
        CurrencyInfo("VES", "Venezuelan Bolivar", "Bs.S", "🇻🇪", "Venezuela", priority = 159),
        CurrencyInfo("ZWL", "Zimbabwean Dollar", "Z$", "🇿🇼", "Zimbabwe", priority = 160)
    )

    // Baseline robust rates against 1 INR (Base: INR)
    private val DEFAULT_INR_RATES = mapOf(
        "INR" to 1.0,
        "USD" to 0.01150,
        "EUR" to 0.01055,
        "GBP" to 0.00898,
        "AED" to 0.04223,
        "SAR" to 0.04312,
        "KWD" to 0.00353,
        "QAR" to 0.04186,
        "CAD" to 0.01582,
        "AUD" to 0.01772,
        "SGD" to 0.01518,
        "JPY" to 1.76200,
        "CNY" to 0.08275,
        "CHF" to 0.01012,
        "OMR" to 0.00442,
        "BHD" to 0.00433,
        "MYR" to 0.05140,
        "THB" to 0.39500,
        "NZD" to 0.01950,
        "HKD" to 0.08980,
        "KRW" to 15.8500,
        "ZAR" to 0.20800,
        "BRL" to 0.06650,
        "RUB" to 1.05500,
        "TRY" to 0.39800,
        "IDR" to 184.200,
        "MXN" to 0.23100,
        "PHP" to 0.65800,
        "VND" to 289.000,
        "PKR" to 3.21000,
        "BDT" to 1.38000,
        "LKR" to 3.42000,
        "NPR" to 1.60000,
        "SEK" to 0.12100,
        "NOK" to 0.12400,
        "DKK" to 0.07860,
        "PLN" to 0.04560,
        "ILS" to 0.04150,
        "EGP" to 0.56500,
        "NGN" to 17.5000,
        "KES" to 1.48000,
        "GHS" to 0.17800,
        "CLP" to 10.9000,
        "COP" to 46.8000,
        "PEN" to 0.04280,
        "ARS" to 11.2000,
        "CZK" to 0.26400,
        "HUF" to 4.19000,
        "RON" to 0.05250,
        "BGN" to 0.02060,
        "TWD" to 0.37200,
        "MAD" to 0.11400,
        "JOD" to 0.00815,
        "BND" to 0.01518,
        "FJD" to 0.02580,
        "MUR" to 0.53500,
        "ISK" to 1.58000,
        "RSD" to 1.23500,
        "UAH" to 0.47800,
        "KZT" to 5.68000,
        "UZS" to 148.500,
        "AZN" to 0.01955,
        "GEL" to 0.03150,
        "AMD" to 4.45000,
        "IQD" to 15.0500,
        "LBP" to 1030.00,
        "TZS" to 29.8000,
        "UGX" to 42.5000,
        "ETB" to 1.42000,
        "DZD" to 1.54000,
        "TND" to 0.03560,
        "CRC" to 5.85000,
        "DOP" to 0.69500,
        "UYU" to 0.48500,
        "BOB" to 0.07950,
        "PYG" to 88.5000,
        "MOP" to 0.09250,
        "MNT" to 39.5000,
        "LAK" to 251.000,
        "KHR" to 46.5000,
        "MMK" to 24.2000,
        "BWP" to 0.15500,
        "NAD" to 0.20800,
        "ZMW" to 0.31500,
        "MZN" to 0.73500,
        "JMD" to 1.82000,
        "TTD" to 0.07800,
        "XCD" to 0.03100,
        "MVR" to 0.17800,
        "BIF" to 33.8000,
        "RWF" to 15.6000,
        "MWK" to 19.8000,
        "GTQ" to 0.08900,
        "HNL" to 0.28800,
        "NIO" to 0.42200,
        "PAB" to 0.01150,
        "AFN" to 0.81500,
        "TJS" to 0.12200,
        "KGS" to 1.00500,
        "CDF" to 32.5000,
        "XOF" to 6.92000,
        "XAF" to 6.92000,
        "ALL" to 1.05000,
        "ANG" to 0.02070,
        "AOA" to 10.5500,
        "AWG" to 0.02070,
        "BAM" to 0.02060,
        "BTN" to 1.00000,
        "BYN" to 0.03750,
        "CUP" to 0.27600,
        "ERN" to 0.17250,
        "GMD" to 0.81500,
        "IRR" to 483.000,
        "KMF" to 5.20000,
        "KPW" to 10.3500,
        "LRD" to 2.24000,
        "LYD" to 0.05580,
        "MGA" to 52.8000,
        "MKD" to 0.65000,
        "MRU" to 0.45800,
        "SDG" to 6.92000,
        "SLE" to 0.26000,
        "SSP" to 15.2000,
        "TVD" to 0.01772,
        "VES" to 0.62500,
        "ZWL" to 0.29800
    )

    private var cachedInrRates: MutableMap<String, Double> = DEFAULT_INR_RATES.toMutableMap()
    var lastUpdatedText: String = "Online Live Rates Initialized"
    var isLiveOnline: Boolean = false

    fun getCurrencyInfo(code: String): CurrencyInfo {
        return ALL_CURRENCIES.find { it.code.equals(code, ignoreCase = true) }
            ?: CurrencyInfo(code.uppercase(), code.uppercase(), code.uppercase(), "🌐", "Global")
    }

    suspend fun fetchLiveRates(): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        try {
            val urlEndpoints = listOf(
                "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/inr.json",
                "https://latest.currency-api.pages.dev/v1/currencies/inr.json",
                "https://open.er-api.com/v6/latest/INR",
                "https://api.exchangerate-api.com/v4/latest/INR"
            )

            var jsonString: String? = null

            for (endpoint in urlEndpoints) {
                try {
                    val url = URL(endpoint)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 7000
                        readTimeout = 7000
                        setRequestProperty("Accept", "application/json")
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Android; CalcPlus/9.0)")
                    }

                    if (conn.responseCode == 200) {
                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        jsonString = reader.readText()
                        reader.close()
                        conn.disconnect()
                        if (!jsonString.isNullOrBlank()) break
                    }
                } catch (e: Exception) {
                    // Try next endpoint
                }
            }

            if (jsonString != null) {
                val json = JSONObject(jsonString)
                val ratesObj = json.optJSONObject("inr") ?: json.optJSONObject("rates")
                if (ratesObj != null) {
                    val newRates = mutableMapOf<String, Double>()
                    val keys = ratesObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val rate = ratesObj.optDouble(key, 0.0)
                        if (rate > 0.0) {
                            newRates[key.uppercase()] = rate
                        }
                    }

                    if (newRates.isNotEmpty()) {
                        newRates["INR"] = 1.0
                        cachedInrRates.putAll(newRates)
                        isLiveOnline = true
                        val timeFormat = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
                        lastUpdatedText = "Live Forex • Updated ${timeFormat.format(Date())}"
                        return@withContext Result.success(cachedInrRates)
                    }
                }
            }
            // If offline, use cache
            isLiveOnline = false
            Result.success(cachedInrRates)
        } catch (e: Exception) {
            isLiveOnline = false
            Result.failure(e)
        }
    }

    fun convert(amount: Double, fromCode: String, toCode: String): Double {
        if (amount == 0.0) return 0.0
        if (fromCode.equals(toCode, ignoreCase = true)) return amount

        val fromRateToInr = if (fromCode.equals("INR", ignoreCase = true)) 1.0 else {
            val rate = cachedInrRates[fromCode.uppercase()] ?: 1.0
            if (rate > 0.0) 1.0 / rate else 1.0
        }

        val inrAmount = amount * fromRateToInr

        val inrToTargetRate = if (toCode.equals("INR", ignoreCase = true)) 1.0 else {
            cachedInrRates[toCode.uppercase()] ?: 1.0
        }

        return inrAmount * inrToTargetRate
    }

    fun getRatesMap(): Map<String, Double> = cachedInrRates

    fun formatIndianRupeeWord(amount: Double): String {
        return when {
            amount >= 10_000_000 -> String.format(Locale.US, "%.2f Crore ₹", amount / 10_000_000.0)
            amount >= 100_000 -> String.format(Locale.US, "%.2f Lakh ₹", amount / 100_000.0)
            amount >= 1_000 -> String.format(Locale.US, "%.2f Thousand ₹", amount / 1_000.0)
            else -> String.format(Locale.US, "%.2f ₹", amount)
        }
    }
}
