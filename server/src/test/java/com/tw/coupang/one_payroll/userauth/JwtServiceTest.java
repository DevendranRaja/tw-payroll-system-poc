package com.tw.coupang.one_payroll.userauth;


public class JwtServiceTest
{
    private static final String TEST_SECRET = "secret-key-for-tests";
    private static final long TEST_EXPIRATION_MS = Long.MAX_VALUE / 2;

//    @BeforeEach
//    void setup() throws Exception {
//        jwtService = new JwtService();
//
//        // Inject test secret
//        Field secretField = JwtService.class.getDeclaredField("secret");
//        secretField.setAccessible(true);
//        secretField.set(jwtService, TEST_SECRET);
//
//        // Inject long expiration
//        Field expField = JwtService.class.getDeclaredField("jwtExpiration");
//        expField.setAccessible(true);
//        expField.set(jwtService, TEST_EXPIRATION_MS);
//    }
}
