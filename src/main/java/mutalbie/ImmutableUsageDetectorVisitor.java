package mutalbie;

import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mauricioaniche on 07/12/2017.
 */
public class ImmutableUsageDetectorVisitor extends ASTVisitor {

    private Context context;
    private Set<String> variablesToMonitor;
    private String currentClazz;
    private String currentMethod;

    public ImmutableUsageDetectorVisitor (Context context) {
        this.context = context;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        this.currentClazz = node.getName().toString();
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {

        variablesToMonitor = new HashSet<>();
        this.currentMethod = node.getName().toString();

        List<SingleVariableDeclaration> parameters = node.parameters();
        for(SingleVariableDeclaration p : parameters) {
            ITypeBinding binding = p.getType().resolveBinding();
            if(binding == null)
                continue;

            if(context.isImmutable(binding.getQualifiedName())) {
                String paramName = p.getName().toString();
                variablesToMonitor.add(paramName);
            }
        }

        return super.visit(node);
    }

    @Override
    public void endVisit(MethodDeclaration node) {
        variablesToMonitor.clear();
        this.currentMethod = null;
    }

    @Override
    public boolean visit(MethodInvocation node) {


        String variableInvoked = node.getExpression().toString();

        if(variablesToMonitor.contains(variableInvoked)) {
            String invokedMethod = node.getName().toString();

            boolean problematic = invokedMethod.startsWith("set");
            context.addProblem(currentClazz, currentMethod);
        }





        return super.visit(node);

    }
}