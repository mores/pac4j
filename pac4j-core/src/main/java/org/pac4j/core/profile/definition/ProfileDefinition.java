package org.pac4j.core.profile.definition;

import org.pac4j.core.profile.AttributeLocation;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.converter.AttributeConverter;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.pac4j.core.profile.AttributeLocation.AUTHENTICATION_ATTRIBUTE;
import static org.pac4j.core.profile.AttributeLocation.PROFILE_ATTRIBUTE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Define a profile (its class and attributes).
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public abstract class ProfileDefinition<P extends CommonProfile> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<String> primaries = new ArrayList<>();

    private final List<String> secondaries = new ArrayList<>();

    private final Map<String, AttributeConverter<? extends Object>> converters = new HashMap<>();

    protected Function<Object[], P> newProfile = parameters -> (P) new CommonProfile();

    /**
     * Return the new built profile.
     *
     * @param parameters some optional input parameters
     * @return the new built profile
     */
    public P newProfile(final Object... parameters) {
        return newProfile.apply(parameters);
    }

    /**
     * Convert a profile attribute, if necessary, and add it to the profile.
     *
     * @param profile the profile
     * @param name the attribute name
     * @param value the attribute value
     * 
     * @deprecated Use {@link #convertAndAdd(CommonProfile, AttributeLocation, String, Object)} instead.
     */
    @Deprecated
    public void convertAndAdd(final CommonProfile profile, final String name, final Object value) {
        convertAndAdd(profile, PROFILE_ATTRIBUTE, name, value);
    }

    /**
     * Convert a profile or authentication attribute, if necessary, and add it to the profile.
     *
     * @param profile The profile.
     * @param attributeLocation Location of the attribute inside the profile: classic profile attribute, authentication attribute, ...
     * @param name The attribute name.
     * @param value The attribute value.
     */
    public void convertAndAdd(final CommonProfile profile, final AttributeLocation attributeLocation, final String name,
            final Object value) {
        if (value != null) {
            final Object convertedValue;
            final AttributeConverter<? extends Object> converter = this.converters.get(name);
            if (converter != null) {
                convertedValue = converter.convert(value);
                if (convertedValue != null) {
                    logger.debug("converted to => key: {} / value: {} / {}", name, convertedValue, convertedValue.getClass());
                }
            } else {
                convertedValue = value;
                logger.debug("no conversion => key: {} / value: {} / {}", name, convertedValue, convertedValue.getClass());
            }

            if (attributeLocation.equals(AUTHENTICATION_ATTRIBUTE)) {
                profile.addAuthenticationAttribute(name, convertedValue);
            } else {
                profile.addAttribute(name, convertedValue);
            }
        }
    }

    /**
     * Convert the profile attributes, if necessary, and add them to the profile.
     * 
     * You may want to use {@link #convertAndAdd(CommonProfile, Map, Map)} which supports adding authentication attributes.
     *
     * @param profile The profile.
     * @param attributes The profile attributes. May be {@code null}.
     * 
     * @deprecated Use {@link #convertAndAdd(CommonProfile, Map, Map)} instead.
     */
    @Deprecated
    public void convertAndAdd(final CommonProfile profile, final Map<String, Object> attributes) {
        convertAndAdd(profile, attributes, null);
    }

    /**
     * Convert the profile and authentication attributes, if necessary, and add them to the profile.
     *
     * @param profile The profile.
     * @param profileAttributes The profile attributes. May be {@code null}.
     * @param authenticationAttributes The authentication attributes. May be {@code null}.
     */
    public void convertAndAdd(final CommonProfile profile,
            final Map<String, Object> profileAttributes,
            final Map<String, Object> authenticationAttributes) {
        if (profileAttributes != null) {
            profileAttributes.entrySet().stream()
                .forEach(entry -> convertAndAdd(profile, PROFILE_ATTRIBUTE, entry.getKey(), entry.getValue()));
        }
        if (authenticationAttributes != null) {
            authenticationAttributes.entrySet().stream()
                .forEach(entry -> convertAndAdd(profile, AUTHENTICATION_ATTRIBUTE, entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Define the way to build the profile.
     *
     * @param profileFactory the way to build the profile
     */
    protected void setProfileFactory(final Function<Object[], P> profileFactory) {
        CommonHelper.assertNotNull("profileFactory", profileFactory);
        this.newProfile = profileFactory;
    }

    /**
     * Add an attribute as a primary one and its converter.
     *
     * @param name name of the attribute
     * @param converter converter
     */
    protected void primary(final String name, final AttributeConverter<? extends Object> converter) {
        primaries.add(name);
        converters.put(name, converter);
    }

    /**
     * Add an attribute as a secondary one and its converter.
     *
     * @param name name of the attribute
     * @param converter converter
     */
    protected void secondary(final String name, final AttributeConverter<? extends Object> converter) {
        secondaries.add(name);
        converters.put(name, converter);
    }

    public List<String> getPrimaryAttributes() {
        return this.primaries;
    }

    public List<String> getSecondaryAttributes() {
        return this.secondaries;
    }

    protected Map<String, AttributeConverter<? extends Object>> getConverters() {
        return converters;
    }
}