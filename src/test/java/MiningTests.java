import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import blockchain.ManagerBehavoir;
import blockchain.WorkerBehavior;
import model.Block;
import model.HashResult;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import utils.BlocksData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MiningTests {
    @Test
    void testEMiningFailsIfNonceNotInRange() {
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
        TestInbox<ManagerBehavoir.Command> testInbox = TestInbox.create(); //Stub of a actor thaqt can receive messages
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, 0, 5, testInbox.getRef());
        testActor.run(message);
        List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();
        assertEquals(logMessages.size() ,1);
        System.out.println(logMessages.get(0).message());
        assertEquals(logMessages.get(0).message(), "null");
        assertEquals(logMessages.get(0).level(), Level.DEBUG);
    }

    @Test
    void testEMiningPassesIfNonceIsInRange() {
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
        TestInbox<ManagerBehavoir.Command> testInbox = TestInbox.create(); //Stub of a actor thaqt can receive messages
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, 2244000, 5, testInbox.getRef());
        testActor.run(message);
        List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();
        assertEquals(logMessages.size() ,1);
        String expectedResult = "2244662 : 00000af3145ae7ecacaea33935550052600d3904c94de67b1d3e07dcf7d1027b";
        System.out.println(logMessages.get(0).message());
        assertEquals(expectedResult, logMessages.get(0).message());
        assertEquals(logMessages.get(0).level(), Level.DEBUG);
    }

    @Test
    void testMessageIsReceivedIfNonceIsInRange(){
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
        TestInbox<ManagerBehavoir.Command> testInbox = TestInbox.create(); //Stub of a actor thaqt can receive messages
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, 2244000, 5, testInbox.getRef());
        testActor.run(message);

        HashResult expectedHashResult = new HashResult();
        expectedHashResult.foundAHash("00000af3145ae7ecacaea33935550052600d3904c94de67b1d3e07dcf7d1027b",2244662);
        List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();
        ManagerBehavoir.Command expectedCommand = new ManagerBehavoir.HasHResultCommand(expectedHashResult);
        testInbox.expectMessage(expectedCommand); //has result must have equal() method
    }

    @Test
    void testNoMessageIsReceivedIfNonceIsNotInRange(){
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
        TestInbox<ManagerBehavoir.Command> testInbox = TestInbox.create(); //Stub of a actor thaqt can receive messages
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, 0, 5, testInbox.getRef());
        testActor.run(message);
        assertFalse(testInbox.hasMessages());
    }

}
