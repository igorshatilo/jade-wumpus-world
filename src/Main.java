import com.wumpus.EnvironmentAgent;
import com.wumpus.NavigatorAgent;
import com.wumpus.SpeleologistAgent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Main {

    public static void main(String[] args) {
        try {
            Runtime runtime = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            AgentContainer container = runtime.createMainContainer(profile);

            // Creating environment agent
            AgentController environmentAgent = container.createNewAgent("environment", EnvironmentAgent.class.getName(), new Object[]{""
                    + ". . . P "
                    + "W G P . "
                    + ". . . . "
                    + "S . P . "});
            environmentAgent.start();

            // Creating navigator agent
            AgentController navigatorAgent = container.createNewAgent("navigator", NavigatorAgent.class.getName(), new Object[]{});
            navigatorAgent.start();

            // Creating speleologist agent
            AgentController speleologistAgent = container.createNewAgent("speleologist", SpeleologistAgent.class.getName(), new Object[]{});
            speleologistAgent.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
