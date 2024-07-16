package io.github.francescodonnini.metrics;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class MethodVisitor extends VoidVisitorAdapter<List<JavaMethod>> {
    private final List<JavaMethod> methods = new ArrayList<>();
    private boolean invalidateResult;

    public List<JavaMethod> getMethods() {
        if (invalidateResult) {
            return List.of();
        }
        return methods;
    }

    @Override
    public void visit(MethodDeclaration n, List<JavaMethod> arg) {
        super.visit(n, arg);
        var name = n.getNameAsString();
        var range = n.getRange();
        var loc = 0;
        if (range.isPresent()) {
            loc = range.get().getLineCount();
            methods.add(new JavaMethod(name, loc));
        } else {
            invalidateResult = true;
        }
    }
}
