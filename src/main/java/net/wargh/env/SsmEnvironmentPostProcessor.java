package net.wargh.env;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;

/**
 * Load properties from ssm parameter store, registered in META-INF/spring.factories
 */
public class SsmEnvironmentPostProcessor implements EnvironmentPostProcessor, ApplicationListener<ApplicationEnvironmentPreparedEvent> {

	private static final String DEFAULT_PROFILE = "";
	private static final String DEFAULT_PREFIX = "";
	
    private static final DeferredLog log = new DeferredLog();

    private final AWSSimpleSystemsManagement ssmClient = AWSSimpleSystemsManagementClientBuilder.defaultClient();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication application) {

        boolean enabled = configurableEnvironment.getProperty("ssm.configuration.enabled", Boolean.class, true);
        
		if (enabled) {
            log.debug("Using ssm configuration");

            String[] profiles = configurableEnvironment.getActiveProfiles();

	        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

	        String prefix = configurableEnvironment.getProperty("ssm.configuration.prefix", DEFAULT_PREFIX);
	        
            propertySources.addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, loadConfiguration(prefix, profiles));

        } else {
            log.debug("Disabled ssm configuration");
        }
    }

    private MapPropertySource loadConfiguration(String prefix, String... profiles) {
		Map<String, Object> properties = new HashMap<String, Object>();
		
		properties.putAll(loadPropertiesForProfile(prefix, DEFAULT_PROFILE));
		
		for (String profile : profiles) {
		    properties.putAll(loadPropertiesForProfile(prefix, profile));
		}
		
		return new MapPropertySource("ssm", properties);
    }

    private Map<String, Object> loadPropertiesForProfile(String prefix, String profile) {
        Map<String, Object> properties = new HashMap<String, Object>();
        
        GetParametersByPathRequest request = new GetParametersByPathRequest().withPath(createPath(prefix, profile)).withWithDecryption(true);

        GetParametersByPathResult result;

        do {
            result = ssmClient.getParametersByPath(request);

            for (Parameter parameter : result.getParameters()) {
                properties.put(removePath(parameter.getName()), parameter.getValue());
            }

            request.setNextToken(result.getNextToken());

        } while (result.getNextToken() != null);

        return properties;
    }

	protected String createPath(final String prefix, final String profile) {
		return profile.equals("") ? "/" + prefix : ("/" + prefix + "/" + profile).replace("//","/");
	}

    protected String removePath(final String fullname) {
        final int pos = fullname.lastIndexOf("/");
        
        return pos == -1 ? fullname : fullname.substring(pos + 1);
    }

	@Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        log.replayTo(SsmEnvironmentPostProcessor.class);
    }
}
