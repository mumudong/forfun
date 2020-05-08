package hive;

/**
 * Driver.run()
 *
 * => HiveDriverRunHook.preDriverRun()(hive.exec.driver.run.hooks)
 *
 * => Driver.compile()
 *
 * => HiveSemanticAnalyzerHook.preAnalyze()(hive.semantic.analyzer.hook)
 *
 * => SemanticAnalyze(QueryBlock, LogicalPlan, PhyPlan, TaskTree)
 *
 * => HiveSemanticAnalyzerHook.postAnalyze()(hive.semantic.analyzer.hook)
 *
 * => QueryString redactor(hive.exec.query.redactor.hooks)
 *
 * => QueryPlan Generation
 *
 * => Authorization
 *
 * => Driver.execute()
 *
 * => ExecuteWithHookContext.run() || PreExecute.run() (hive.exec.pre.hooks)
 *
 * => TaskRunner
 *
 * => if failed, ExecuteWithHookContext.run()(hive.exec.failure.hooks)
 *
 * => ExecuteWithHookContext.run() || PostExecute.run() (hive.exec.post.hooks)
 *
 * => HiveDriverRunHook.postDriverRun()(hive.exec.driver.run.hooks)
 *
 *
 */
public class HookTest {
}
