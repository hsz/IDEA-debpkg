package com.github.nthykier.debpkg.deb822;

import com.github.nthykier.debpkg.deb822.field.Deb822KnownFieldValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.nthykier.debpkg.deb822.Deb822YamlDataFileParserUtil.*;

public class Deb822KnownFieldsAndValues {

    private static final Map<String, Deb822KnownField> KNOWN_FIELDS = new HashMap<>();
    private static List<String> KNOWN_FIELD_NAMES = Collections.emptyList();

    private Deb822KnownFieldsAndValues() {}

    @Nullable
    public static Deb822KnownField lookupDeb822Field(@NotNull String fieldName) {
        return lookupDeb822Field(fieldName, null);
    }

    @Contract("_, !null -> !null")
    public static Deb822KnownField lookupDeb822Field(@NotNull String fieldName, @Nullable Deb822KnownField fallback) {
        return KNOWN_FIELDS.getOrDefault(fieldName.toLowerCase(), fallback);
    }

    @NotNull
    public static List<String> getAllKnownFieldNames() {
        if (KNOWN_FIELD_NAMES.size() != KNOWN_FIELDS.size()) {
            // When 1.10 can be assumed; use ".collect(Collectors.toUnmodifiableList())"
            KNOWN_FIELD_NAMES = Collections.unmodifiableList(KNOWN_FIELDS.values()
                    .stream()
                    .map(Deb822KnownField::getCanonicalFieldName)
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.toList())
            );
        }
        return KNOWN_FIELD_NAMES;
    }

    static void ADD_KNOWN_FIELDS(String ... fieldNames) {
        for (String fieldName : fieldNames) {
            String fieldLc = fieldName.toLowerCase().intern();
            Deb822KnownField field = new Deb822KnownFieldImpl(fieldName, false,
                    Collections.emptyNavigableSet(), null);
            checkedAddField(fieldLc, field);
        }
    }

    private static void checkedAddField(String fieldNameLC, Deb822KnownField field) {
        Deb822KnownField existing = KNOWN_FIELDS.putIfAbsent(fieldNameLC, field);
        assert existing == null : "Field " + existing.getCanonicalFieldName() + " is declared twice";
    }

    public static class Deb822KnownFieldImpl implements Deb822KnownField {
        private final String canonicalFieldName;
        private final boolean areAllKeywordsKnown;
        private final boolean hasKnownValues;
        private final NavigableSet<String> allKnownKeywords;
        private final String docs;

        public Deb822KnownFieldImpl(@NotNull String canonicalFieldName, boolean areAllKeywordsKnown,
                                    @NotNull NavigableSet<String> allKnownKeywords,
                                    String docs) {
            this.canonicalFieldName = canonicalFieldName;
            this.areAllKeywordsKnown = areAllKeywordsKnown;
            this.allKnownKeywords = Collections.unmodifiableNavigableSet(allKnownKeywords) ;
            this.hasKnownValues = areAllKeywordsKnown || !this.allKnownKeywords.isEmpty();
            this.docs = docs;
        }

        @NotNull
        @Override
        public String getCanonicalFieldName() {
            return canonicalFieldName;
        }

        @Override
        public boolean areAllKeywordsKnown() {
            return areAllKeywordsKnown;
        }


        @Override
        public boolean hasKnownValues() {
            return hasKnownValues;
        }

        @NotNull
        @Override
        public NavigableSet<String> getKnownKeywords() {
            return allKnownKeywords;
        }

        @Nullable
        @Override
        public String getFieldDescription() {
            return this.docs;
        }
    }

    private static void loadKnownFieldDefinitions() {
        InputStream s = Deb822KnownFieldsAndValues.class.getResourceAsStream("DebianControl.data.yaml");
        Yaml y = new Yaml();
        Map<String, Object> data = y.load(s);
        List<Map<String, Object>> fieldDefinitions = getList(data, "fields");
        for (Map<String, Object> fieldDefinition : fieldDefinitions) {
            Deb822KnownField field = parseKnownFieldDefinition(fieldDefinition);
            checkedAddField(field.getCanonicalFieldName().toLowerCase().intern(), field);
        }
    }

    private static Deb822KnownField parseKnownFieldDefinition(Map<String, Object> fieldDef) {
        String canonicalName = getRequiredString(fieldDef, "canonicalName");
        Deb822KnownFieldValueType valueType = Deb822KnownFieldValueType.valueOf(
                getOptionalString(fieldDef, "valueType", "FREE_TEXT_VALUE")
        );
        List<String> keywordList = getList(fieldDef, "keywordList");
        String docs = getOptionalString(fieldDef, "description", null);
        boolean allKeywordsKnown = false;
        switch (valueType) {
            case SINGLE_TRIVIAL_VALUE:
                if (!keywordList.isEmpty()) {
                    throw new IllegalArgumentException("Field " + canonicalName + " has keywords but is a SINGLE_VALUE"
                    + " (should it have been a SINGLE_KEYWORD instead?)");
                }
                break;
            case SINGLE_KEYWORD:
                if (keywordList.isEmpty()) {
                    throw new IllegalArgumentException("Field " + canonicalName + " has no keywords but is a SINGLE_KEYWORD"
                            + " (should it have been a SINGLE_VALUE instead?)");
                }
                break;
            case COMMA_SEPARATED_VALUE_LIST:
            case SPACE_SEPARATED_VALUE_LIST:
                /* OK with or without keywords */
                break;
            case FREE_TEXT_VALUE:
                if (!keywordList.isEmpty()) {
                    throw new IllegalArgumentException("Field " + canonicalName + " has keywords but is a FREE_TEXT_VALUE");
                }
                break;
            default:
                throw new AssertionError("Unsupported but declared valueType: " + valueType);
        }
        if (!keywordList.isEmpty()) {
            allKeywordsKnown = true;
            if (keywordList.get(keywordList.size() - 1).equals("...")) {
                allKeywordsKnown = false;
                keywordList.remove(keywordList.size() - 1);
            }
        }
        return new Deb822KnownFieldImpl(canonicalName, allKeywordsKnown, new TreeSet<>(keywordList), docs);
    }

    static {
        /* Fields with structured content we know but currently cannot validate at the moment */
        ADD_KNOWN_FIELDS("Vcs-Git", "Vcs-Svn", "Vcs-Browser");
        ADD_KNOWN_FIELDS(
                "Build-Depends", "Build-Depends-Indep", "Build-Depends-Arch",
                "Build-Conflicts", "Build-Conflicts-Indep", "Build-Conflicts-Arch",
                "Pre-Depends", "Depends", "Recommends", "Suggests", "Enhances",
                "Conflicts", "Replaces", "Breaks"
                );

        loadKnownFieldDefinitions();
    }
}

