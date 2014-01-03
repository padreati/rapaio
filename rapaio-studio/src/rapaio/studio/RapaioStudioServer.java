package rapaio.studio;

import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;
import rapaio.workspace.Workspace;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RapaioStudioServer implements ApplicationComponent {

    private static RapaioStudioServer instance;

    public static RapaioStudioServer getInstance() {
        if (instance == null) {
            instance = new RapaioStudioServer();
        }
        return instance;
    }

    private final AgregatePrinter agregatePrinter = new AgregatePrinter();

    private RapaioStudioServer() {
    }

    public void initComponent() {
        Workspace.setPrinter(agregatePrinter);
    }

    public void disposeComponent() {

    }

    public AgregatePrinter getAgregatePrinter() {
        return agregatePrinter;
    }

    @NotNull
    public String getComponentName() {
        return "rapaio.studio.RapaioStudioServer";
    }
}
