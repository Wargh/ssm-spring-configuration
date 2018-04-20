package net.wargh.env;

import static org.junit.Assert.*;

import org.junit.Test;

public class SsmEnvironmentPostProcessorTest {

	private SsmEnvironmentPostProcessor test = new SsmEnvironmentPostProcessor();
	
	@Test
	public void testDefaultPath() {
		assertEquals("/", test.createPath("", ""));
	}
	
	@Test
	public void testNoPrefixProfile() {
		assertEquals("/profile", test.createPath("", "profile"));
	}
	
	@Test
	public void testDefaultPrefix() {
		assertEquals("/test-app", test.createPath("test-app", ""));
	}
	
	@Test
	public void testPrefixWithProfile() {
		assertEquals("/test-app/profile", test.createPath("test-app", "profile"));
	}
	
	@Test
	public void testSimpleKey() {
		assertEquals("simplekey", test.removePath("simplekey"));
	}
	
	@Test
	public void testCompoundKeyWithNoPath() {
		assertEquals("compound.key", test.removePath("compound.key"));
	}
	
	@Test
	public void testCompoundKeyWithPath() {
		assertEquals("compound.key", test.removePath("/test-app/profile/compound.key"));
	}
}
