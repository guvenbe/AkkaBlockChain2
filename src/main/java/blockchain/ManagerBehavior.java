package blockchain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import model.Block;
import model.HashResult;

import java.io.Serializable;
import java.util.Objects;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    public interface Command extends Serializable {
    }

    public static class MineBlockCommand implements Command {
        public static final long serialVersionUID = 1L;
        private Block block;
        private ActorRef<HashResult> sender;
        private int difficulty;

        public MineBlockCommand(Block block, ActorRef<HashResult> sender, int difficulty) {
            this.block = block;
            this.sender = sender;
            this.difficulty = difficulty;
        }

        public Block getBlock() {
            return block;
        }

        public ActorRef<HashResult> getSender() {
            return sender;
        }

        public int getDifficulty() {
            return difficulty;
        }
    }

    public static class HasHResultCommand implements Command{
        public static final long serialVersionUID = 1L;
        private HashResult hashResult;

        public HasHResultCommand(HashResult hashResult) {
            this.hashResult = hashResult;
        }

        public HashResult getHashResult() {
            return hashResult;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HasHResultCommand)) return false;
            HasHResultCommand that = (HasHResultCommand) o;
            return getHashResult().equals(that.getHashResult());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getHashResult());
        }
    }
    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive(){
        return idleMessageHandler();
    }

    public Receive<Command> idleMessageHandler() {
        return newReceiveBuilder()

                .onMessage(MineBlockCommand.class, message -> {
                    this.sender=message.getSender();
                    this.block=message.getBlock();
                    this.difficulty= message.getDifficulty();
                    this.currentlyMining =true;

                    for (int i = 0; i < 10; i++) {
                        startNextWorker();
                    }
                    return activeMessageHandler();
                })

                .build();
    }

    public Receive<Command> activeMessageHandler(){
        return newReceiveBuilder()
                .onSignal(Terminated.class, handler ->{ //for the watch we need to handle the termination message
                    //AVOIDS DEATH PACT exception
                    startNextWorker();
                    return Behaviors.same();
                })
                .onMessage(HasHResultCommand.class, message->{
                    for (ActorRef<Void> child : getContext().getChildren()) {
                        getContext().stop(child);
                    }
                    this.currentlyMining=false;
                    sender.tell(message.getHashResult());
                    return idleMessageHandler();
                })
                .build();
    }

    private ActorRef<HashResult> sender;
    private Block block;
    private int difficulty;
    private int currentNonce = 0;
    private boolean currentlyMining;

    private void startNextWorker() {
        if (currentlyMining) {
            //System.out.println("About to start mining with nonces starting at " + currentNonce * 1000);
            Behavior<WorkerBehavior.Command> workerBehavior =
                    Behaviors.supervise(WorkerBehavior.create()).onFailure(SupervisorStrategy.resume());

            ActorRef<WorkerBehavior.Command> worker = getContext().spawn(workerBehavior, "worker" + currentNonce);
            getContext().watch(worker); //supervsion for each worker
            worker.tell(new WorkerBehavior.Command(block, currentNonce * 1000, difficulty, getContext().getSelf()));
            currentNonce++;
        }
    }
}
