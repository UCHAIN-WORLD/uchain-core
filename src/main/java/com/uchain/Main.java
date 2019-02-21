package com.uchain;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.uchain.main.Settings;
import com.uchain.network.Node;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger("Main");
    public static void main(String[] args) {
        Settings settings = null;
        try {
            ArgumentParser parser = ArgumentParsers.newFor("Main").build()//Argparse4j 解析命令行参数
                    .defaultHelp(true)
                    .description("params customer definition.");
            parser.addArgument("-p", "--port");
            parser.addArgument("--config").required(true).help("files for configuration");
            Namespace ns = null;
            try {
                ns = parser.parseArgs(args);
            } catch (ArgumentParserException e) {
                parser.handleError(e);
                System.exit(2);
            }

            try {
                String port = ns.getString("port");
                String configFile = ns.getString("config");

                settings = new Settings(configFile);
                Settings.getSysteProperties(configFile);

                if(port!= null && !port.isEmpty()){
                        logger.info("port is: "+ port);
                    settings.updatePort(port);
                }

                logger.info("configFile is exist: "+ StringUtils.isNotEmpty(configFile));
            }
            catch (java.text.ParseException e){
                e.printStackTrace();
            }

            ActorSystem uchainSystem = ActorSystem.create("uchainSystem");

            ActorRef nodeActor = uchainSystem.actorOf(Node.props(settings), "nodeManager");
            try {
                JerseyServer.runServer(nodeActor,settings);
                System.out.println("OK");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
