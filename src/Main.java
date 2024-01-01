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
            AgentController environmentAgent = container.createNewAgent("environment", EnvironmentAgent.class.getName(), new Object[]{});
            environmentAgent.start();

            AgentController navigatorAgent = container.createNewAgent("navigator", NavigatorAgent.class.getName(), new Object[]{});
            navigatorAgent.start();

            AgentController speleologistAgent = container.createNewAgent("speleologist", SpeleologistAgent.class.getName(), new Object[]{});
            speleologistAgent.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
