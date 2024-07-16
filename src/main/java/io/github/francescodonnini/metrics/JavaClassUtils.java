package io.github.francescodonnini.metrics;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaClassUtils {
    private static final Logger logger = Logger.getLogger(JavaClassUtils.class.getName());
    private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    private JavaClassUtils() {}

    public static List<JavaMethod> getMethods(Path path) {
        try (final var fileMan = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            final var cu = fileMan.getJavaFileObjects(path);
            final var task = (JavacTask) compiler.getTask(null, fileMan, null, null, null, cu);
            final var trees = task.parse();
            final var cuTree = trees.iterator().next();
            final var classTree = (ClassTree) cuTree.getTypeDecls().getFirst();
            final var srcPositions = Trees.instance(task).getSourcePositions();
            final var members = classTree.getMembers();
            final var methods = members.stream()
                    .filter(MethodTree.class::isInstance)
                    .map(MethodTree.class::cast)
                    .toList();
            final var javaMethods = new ArrayList<JavaMethod>();
            methods.forEach(m -> {
                var start = srcPositions.getStartPosition(cuTree, m);
                var end = srcPositions.getEndPosition(cuTree, m);
                javaMethods.add(new JavaMethod(m.getName().toString(), end - start));

            });
            return javaMethods;
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (NoSuchElementException e) {
            logger.log(Level.INFO, "empty class %s%n".formatted(path));
        }
        return List.of();
    }
}
