package net.ssehub.csvmerger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessedFunctionCommits {
    
    static class Function {
        private String path;
        private String function;
        private String line;
        
        public Function(String path, String function, String line) {
            this.path = path;
            this.function = function;
            this.line = line;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((function == null) ? 0 : function.hashCode());
            result = prime * result + ((line == null) ? 0 : line.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object other) {
            boolean isEqual = false;
            
            if (other instanceof Function) {
                Function otherFunc = (Function) other;
                isEqual = stringsEqual(this.path, otherFunc.path);
                isEqual &= stringsEqual(this.function, otherFunc.function);
                isEqual &= stringsEqual(this.line, otherFunc.line);
            }
            
            return isEqual;
        }

        private boolean stringsEqual(String str1, String str2) {
            boolean isEqual = false;
            if (str1 == null && str2 == null) {
                isEqual = true;
            } else if (str1 == null) {
                isEqual = false;
            } else {
                isEqual = str1.equals(str2);
            }
            
            return isEqual;
        }
    }
    
    private Map<String, Set<Function>> processedFuncs = new HashMap<>();

    public boolean functionProcessed(String commit, String path, String function, String line) {
        Set<Function> functionsOfCommit = processedFuncs.get(commit);
        if (null == functionsOfCommit) {
            // Add if it does not exist
            functionsOfCommit = new HashSet<>();
            processedFuncs.put(commit, functionsOfCommit);
        }
        
        // Now comes the check
        Function func = new Function(path, function, line);
        boolean alreadyExistent = functionsOfCommit.contains(func);
        if (!alreadyExistent) {
            // Add to map
            functionsOfCommit.add(func);
        }
        
        return alreadyExistent;
    }
    
}
