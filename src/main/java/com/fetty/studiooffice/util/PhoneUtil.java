package com.fetty.studiooffice.util;


import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Класс для валидации и преобразования номеров телефонов
 *
 *
 */
public class PhoneUtil {

    /** Код_страны :: Длина_номера */
    public final static Map<String, Integer> COUNTRY_CODE_LENGTH_MAP = initCountryCodeLengthMap();

    private static Map<String, Integer> initCountryCodeLengthMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("380", 12);
        map.put("373", 11);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Преобразовывает введённый номер телефона в международный формат, начиная со знака '+',
     * а дальше все цифры без пробелов.
     * Например, '+380501112233'.
     */
    public static String toInternationalPhoneNumberFormatWithPlus(String phoneNumber) {
        return "+" + toInternationalPhoneNumberFormat(phoneNumber);
    }

    /**
     * Преобразовывает введённый номер телефона в международный формат, без знака '+',
     * а дальше все цифры без пробелов.
     * Например, '380501112233'.
     */
    public static String toInternationalPhoneNumberFormat(String phoneNumber) {
        if (!validatePhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number: '" + phoneNumber + "'");
        }
        return normalizePhone(phoneNumber);
    }

    /**
     * Проводит валидацию номера телефона.
     * Все символы, кроме цифр, игнорируются. Количество цифр должно быть от 9 до 13,
     * а для заложенных стран (Украины и Молдовы) - строго соответствовать номинальному (12 и 11 соответственно).
     * @return true, если номер телефона удовлетворяет условиям
     */
    public static boolean validatePhoneNumber(String phoneNumber) {
        if (StringUtils.isNotBlank(phoneNumber)) {
            String normalizedPhone = normalizePhone(phoneNumber);
            if (normalizedPhone.matches("[0-9]{9,13}")) {
                for (Map.Entry<String, Integer> entry : COUNTRY_CODE_LENGTH_MAP.entrySet()) {
                    if (normalizedPhone.startsWith(entry.getKey())) {
                        return normalizedPhone.length() == entry.getValue();
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static String normalizePhone(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9]*", ""); // Оставляем только цифры
    }

}
