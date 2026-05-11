package com.kevinmck91.kindlebot.logging;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

	/*
	 * private final Logger logger = LoggerFactory.getLogger(this.getClass());
	 * 
	 * // Pointcut to match all public methods in your packages
	 * 
	 * @Pointcut("execution(public * com.kevinmck91.kindlebot..*(..))") public void
	 * applicationPackagePointcut() { }
	 * 
	 * @Before("applicationPackagePointcut()") public void logBefore(JoinPoint
	 * joinPoint) { logger.info("➡️ Entering: {} with args {}",
	 * joinPoint.getSignature(), joinPoint.getArgs()); }
	 * 
	 * @AfterReturning(pointcut = "applicationPackagePointcut()", returning =
	 * "result") public void logAfter(JoinPoint joinPoint, Object result) {
	 * logger.info("✅ Exiting: {} with result {}", joinPoint.getSignature(),
	 * result); }
	 * 
	 * @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "ex")
	 * public void logException(JoinPoint joinPoint, Throwable ex) {
	 * logger.error("❌ Exception in {} with message {}", joinPoint.getSignature(),
	 * ex.getMessage(), ex); }
	 * 
	 * @Around("applicationPackagePointcut()") public Object
	 * logExecutionTimeAndDetails(ProceedingJoinPoint joinPoint) throws Throwable {
	 * long start = System.currentTimeMillis();
	 * 
	 * String method = joinPoint.getSignature().toShortString(); Object[] args =
	 * joinPoint.getArgs();
	 * 
	 * logger.info("➡️ Entering {}", method); if (args.length > 0) {
	 * logger.info("🧾 Args: {}", Arrays.toString(args)); }
	 * 
	 * try { Object result = joinPoint.proceed();
	 * 
	 * long duration = System.currentTimeMillis() - start;
	 * logger.info("✅ Exiting {} | ⏱ Took {} ms", method, duration);
	 * 
	 * // If the method is fetching API data (like GithubItem[]) if (result
	 * instanceof GithubItem[]) { GithubItem[] items = (GithubItem[]) result;
	 * logger.info("📦 API call returned {} items", items.length); } else if (result
	 * instanceof ResponseEntity) { ResponseEntity<?> response = (ResponseEntity<?>)
	 * result; logger.info("📡 HTTP Response: status={}, body={}",
	 * response.getStatusCode(), response.getBody()); }
	 * 
	 * return result;
	 * 
	 * } catch (Throwable ex) { logger.error("❌ Exception in {}: {}", method,
	 * ex.getMessage(), ex); throw ex; }
	 * 
	 * }
	 */

}