package org.sagebionetworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *  To test methods in StackConfigurationImpl that are not directly related to the stack
 *
 *  This class is missing a lot of tests! Please add them :)
 */
@ExtendWith(MockitoExtension.class)
public class StackConfigurationImplUnitTest {
	
	StackConfigurationImpl config;

	@Mock
	ConfigurationProperties mockProperties;
	
	@Mock
	StackEncrypter stackEncrypter;
	
	@BeforeEach
	public void before() {
		this.config = new StackConfigurationImpl(mockProperties, stackEncrypter);
	}

	@Test
	public void testGetDoiPrefixOnProd() {
		String stackName = "prod";
		String prefix = "a prefix";
		when(mockProperties.getProperty(StackConstants.STACK_PROPERTY_NAME)).thenReturn(stackName); // config.isProduction() will return true
		String doiPrefixPropertyName = "org.sagebionetworks.doi.prefix";
		when(mockProperties.getProperty(DOI_PREFIX_PROPERTY)).thenReturn(prefix);

		// Call under test
		String actualPrefix = config.getDoiPrefix();

		assertEquals(prefix, actualPrefix);
		verify(mockProperties).getProperty(StackConstants.STACK_PROPERTY_NAME);
		verify(mockProperties, never()).getProperty(StackConstants.STACK_INSTANCE_PROPERTY_NAME);
		verify(mockProperties).getProperty(doiPrefixPropertyName);

	}

	@Test
	public void testGetDoiPrefixNotOnProd() {
		String stackPropertyName = "testNotProd";
		String stackInstancePropertyName = "testStack";
		String prefix = "a prefix";
		when(mockProperties.getProperty(StackConstants.STACK_PROPERTY_NAME)).thenReturn(stackPropertyName); // config.isProduction() will return false
		when(mockProperties.getProperty(StackConstants.STACK_INSTANCE_PROPERTY_NAME)).thenReturn(stackInstancePropertyName);
		String doiPrefixPropertyName = "org.sagebionetworks.doi.prefix";
		when(mockProperties.getProperty(doiPrefixPropertyName)).thenReturn(prefix);

		String expectedPrefix = prefix + "/" + stackInstancePropertyName;

		// Call under test
		String actualPrefix = config.getDoiPrefix();

		assertEquals(expectedPrefix, actualPrefix);
		verify(mockProperties).getProperty(StackConstants.STACK_PROPERTY_NAME);
		verify(mockProperties).getProperty(StackConstants.STACK_INSTANCE_PROPERTY_NAME);
		verify(mockProperties).getProperty(doiPrefixPropertyName);
	}
	
	@Test
	public void testGetSharedS3BackupBucket() {
		String stackName = "prod";
		when(mockProperties.getProperty(StackConstants.STACK_PROPERTY_NAME)).thenReturn(stackName);
		when(mockProperties.getProperty("org.sagebionetworks.shared.s3.backup.bucket")).thenReturn(".bucket");
		assertEquals("prod.bucket", config.getSharedS3BackupBucket());
	}


	private static final String DOI_PREFIX_PROPERTY = "org.sagebionetworks.doi.prefix";

	@Test
	public void testGetTempCredentialsIamRoleArn() {
		when(mockProperties.hasProperty(StackConfigurationImpl.CONFIG_KEY_STS_IAM_ARN)).thenReturn(true);
		when(mockProperties.getProperty(StackConfigurationImpl.CONFIG_KEY_STS_IAM_ARN)).thenReturn("dummy-arn");

		// Method under test
		String result = config.getTempCredentialsIamRoleArn();
		assertEquals("dummy-arn", result);
	}

	@Test
	public void testGtTempCredentialsIamRoleArn_NullConfig() {
		when(mockProperties.hasProperty(StackConfigurationImpl.CONFIG_KEY_STS_IAM_ARN)).thenReturn(false);

		// Method under test
		String result = config.getTempCredentialsIamRoleArn();
		assertNull(result);

		verify(mockProperties, never()).getProperty(any());
	}
	
	@Test
	public void testGetServiceAuthKey() {
		String serviceName = "someService";
		
		// Call under test
		config.getServiceAuthKey(serviceName);
		
		verify(stackEncrypter).getDecryptedProperty("org.sagebionetworks." + serviceName + ".auth.key");
	}
	
	@Test
	public void testGetServiceAuthSecret() {
		String serviceName = "someService";
		
		// Call under test
		config.getServiceAuthSecret(serviceName);
		
		verify(stackEncrypter).getDecryptedProperty("org.sagebionetworks." + serviceName + ".auth.secret");
	}
	
	@Test
	public void testGetCloudMailInUser() {
		
		// Call under test
		config.getCloudMailInUser();
		
		verify(stackEncrypter).getDecryptedProperty("org.sagebionetworks." + StackConfiguration.SERVICE_CLOUDMAILIN + ".auth.key");
	}
	
	@Test
	public void testGetCloudMailInPassword() {
		
		// Call under test
		config.getCloudMailInPassword();
		
		verify(stackEncrypter).getDecryptedProperty("org.sagebionetworks." + StackConfiguration.SERVICE_CLOUDMAILIN + ".auth.secret");
	}
	
	@Test
	public void testGetDockerRegistryUser() {
		
		// Call under test
		config.getDockerRegistryUser();
		
		verify(stackEncrypter).getDecryptedProperty("org.sagebionetworks." + StackConfiguration.SERVICE_DOCKER_REGISTRY + ".auth.key");
	}
	
	@Test
	public void testGetDockerRegistryPassword() {
		
		// Call under test
		config.getDockerRegistryPassword();
		
		verify(stackEncrypter).getDecryptedProperty("org.sagebionetworks." + StackConfiguration.SERVICE_DOCKER_REGISTRY + ".auth.secret");
	}
	
	@Test
	public void testGetRepositoryServiceProdEndpoint() {
		
		// Call under test
		config.getRepositoryServiceProdEndpoint();
		
		verify(mockProperties).getProperty("org.sagebionetworks.repositoryservice.endpoint.prod");
		
	}

	@Test
	public void testGetCloudFrontPrivateKey() {

		// Call under test
		config.getCloudFrontPrivateKey();

		verify(stackEncrypter).getDecryptedProperty("org.sagebionetworks.cloudfront.private.key.secret");

	}

	@Test
	public void testGetCloudFrontKeyPairId() {

		// Call under test
		config.getCloudFrontKeyId();

		verify(stackEncrypter).getDecryptedProperty("org.sagebionetworks.cloudfront.private.key.id");

	}

	@Test
	public void testGetCloudFrontDomainName() {

		// Call under test
		config.getCloudFrontDomainName();

		verify(mockProperties).getProperty("org.sagebionetworks.cloudfront.domainname");

	}

	// Verifies a prod stack gets the prod static content CDN when no explicit base URL is configured.
	@Test
	public void testGetEmailImagesBaseUrlWithProdStack() {
		when(mockProperties.hasProperty("org.sagebionetworks.email.images.base.url")).thenReturn(false);
		when(mockProperties.getProperty(StackConstants.STACK_PROPERTY_NAME)).thenReturn("prod");
		when(mockProperties.getProperty("org.sagebionetworks.email.images.base.url.template")).thenReturn("https://static.%s.sagebase.org/email-images");

		// Call under test
		assertEquals("https://static.prod.sagebase.org/email-images", config.getEmailImagesBaseUrl());
	}

	// Verifies a dev stack gets the dev static content CDN when no explicit base URL is configured.
	@Test
	public void testGetEmailImagesBaseUrlWithDevStack() {
		when(mockProperties.hasProperty("org.sagebionetworks.email.images.base.url")).thenReturn(false);
		when(mockProperties.getProperty(StackConstants.STACK_PROPERTY_NAME)).thenReturn("dev");
		when(mockProperties.getProperty("org.sagebionetworks.email.images.base.url.template")).thenReturn("https://static.%s.sagebase.org/email-images");

		// Call under test
		assertEquals("https://static.dev.sagebase.org/email-images", config.getEmailImagesBaseUrl());
	}

	// Verifies a stack other than prod uses the dev CDN, since that is the only non-prod static content CDN.
	@Test
	public void testGetEmailImagesBaseUrlWithOtherStack() {
		when(mockProperties.hasProperty("org.sagebionetworks.email.images.base.url")).thenReturn(false);
		when(mockProperties.getProperty(StackConstants.STACK_PROPERTY_NAME)).thenReturn("tst");
		when(mockProperties.getProperty("org.sagebionetworks.email.images.base.url.template")).thenReturn("https://static.%s.sagebase.org/email-images");

		// Call under test
		assertEquals("https://static.dev.sagebase.org/email-images", config.getEmailImagesBaseUrl());
	}

	// Verifies an explicitly configured base URL takes precedence over the stack based template.
	@Test
	public void testGetEmailImagesBaseUrlWithOverride() {
		when(mockProperties.hasProperty("org.sagebionetworks.email.images.base.url")).thenReturn(true);
		when(mockProperties.getProperty("org.sagebionetworks.email.images.base.url")).thenReturn("https://static.prod.sagebase.org/email-images");

		// Call under test
		assertEquals("https://static.prod.sagebase.org/email-images", config.getEmailImagesBaseUrl());

		verify(mockProperties, never()).getProperty("org.sagebionetworks.email.images.base.url.template");
	}

	// Verifies trailing slashes are removed so that appending "/<folder>/<file>" never yields a double slash.
	@Test
	public void testGetEmailImagesBaseUrlWithTrailingSlash() {
		when(mockProperties.hasProperty("org.sagebionetworks.email.images.base.url")).thenReturn(true);
		when(mockProperties.getProperty("org.sagebionetworks.email.images.base.url")).thenReturn("https://static.dev.sagebase.org/email-images//");

		// Call under test
		assertEquals("https://static.dev.sagebase.org/email-images", config.getEmailImagesBaseUrl());
	}

	// Verifies a blank override is returned as an empty string rather than failing the caller.
	@Test
	public void testGetEmailImagesBaseUrlWithBlankValue() {
		when(mockProperties.hasProperty("org.sagebionetworks.email.images.base.url")).thenReturn(true);
		when(mockProperties.getProperty("org.sagebionetworks.email.images.base.url")).thenReturn("  ");

		// Call under test
		assertEquals("", config.getEmailImagesBaseUrl());
	}

	// Verifies an override made up only of slashes ends up as an empty string.
	@Test
	public void testGetEmailImagesBaseUrlWithOnlySlashes() {
		when(mockProperties.hasProperty("org.sagebionetworks.email.images.base.url")).thenReturn(true);
		when(mockProperties.getProperty("org.sagebionetworks.email.images.base.url")).thenReturn("//");

		// Call under test
		assertEquals("", config.getEmailImagesBaseUrl());
	}

	@Test
	public void testGetMarkdownServiceEndpointFromTemplate() {
		when(mockProperties.getProperty("org.sagebionetworks.markdown.service.endpoint.template")).thenReturn("https://md2html.%s.sagebase.org/md2html");
		when(mockProperties.getProperty("org.sagebionetworks.stack")).thenReturn("prod");
		assertEquals("https://md2html.prod.sagebase.org/md2html", config.getMarkdownServiceEndpoint());
	}

}
