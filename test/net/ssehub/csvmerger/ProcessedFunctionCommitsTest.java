package net.ssehub.csvmerger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.csvmerger.ProcessedFunctionCommits;
import net.ssehub.csvmerger.ProcessedFunctionCommits.Function;


public class ProcessedFunctionCommitsTest {
    public ProcessedFunctionCommitsTest() {
        
    }
    
    @Test
    public void testFunctionProcessed() {
        ProcessedFunctionCommits commits = new ProcessedFunctionCommits();
        assertThat(commits.functionProcessed("a", "b", "c", "d"), is(false));
        assertThat(commits.functionProcessed("a", "b", "c", "d"), is(true));
    }
    
    @Test
    public void testFunctionProcessedWithNull() {
        ProcessedFunctionCommits commits = new ProcessedFunctionCommits();
        assertThat(commits.functionProcessed("foo", "bar", "baz", null), is(false));
        assertThat(commits.functionProcessed("foo", "bar", "baz", null), is(true));
    }
    
    @Test
    public void testFunctionProcessedNullCommit() {
        ProcessedFunctionCommits commits = new ProcessedFunctionCommits();
        assertThat(commits.functionProcessed(null, "foo", "bar", "blubb"), is(false));
        assertThat(commits.functionProcessed(null, "foo", "bar", "blubb"), is(true));
    }
    
    @Test
    public void testFunctionProcessedNullPath() {
        ProcessedFunctionCommits commits = new ProcessedFunctionCommits();
        assertThat(commits.functionProcessed("foo", null, "bar", "blubb"), is(false));
        assertThat(commits.functionProcessed("foo", null, "asdfkasdfas", "blubb"), is(false));
        assertThat(commits.functionProcessed("foo", "asdfkasdfas", null, "blubb"), is(false));
    }
    
    @Test
    public void testFunctionEquals() {
        Function f1 = new Function("foo", "bar", "blubb");
        Function f2 = new Function("foo", "bar", "blubb");
        assertThat(f1, is(f2));
    }
    
    @Test
    public void testFunctionEqualsWithNullFunction() {
        Function f1 = new Function("foo", null, "blubb");
        Function f2 = new Function("foo", "bar", "blubb");
        assertThat(f1.equals(f2), is(false));
    }
    
    @Test
    public void testFunctionEqualsWithNull() {
        Function f1 = new Function(null, "bar", "blubb");
        Function f2 = null;
        assertThat(f1.equals(f2), is(false));
    }
}
