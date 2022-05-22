package blockchain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import model.Block;
import model.HashResult;

import java.io.Serializable;
import java.util.Objects;

public class ManagerBehavoir extends AbstractBehavior<ManagerBehavoir.Command> {

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
    private ManagerBehavoir(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavoir::new);
    }

    @Override
    public Receive createReceive() {
        return newReceiveBuilder()
                .onMessage(MineBlockCommand.class, message -> {
                    this.sender=message.getSender();
                    this.block=message.getBlock();
                    this.difficulty= message.getDifficulty();
                    for (int i = 0; i < 10; i++) {
                        startNextWorker();
                    }
                    return Behaviors.same();
                })
                .build();
    }

    private ActorRef<HashResult> sender;
    private Block block;
    private int difficulty;
    private int currentNonce = 0;

    private void startNextWorker() {
        ActorRef<WorkerBehavior.Command> worker = getContext().spawn(WorkerBehavior.create(), "worker" + currentNonce);
        worker.tell(new WorkerBehavior.Command(block, currentNonce * 1000, difficulty, getContext().getSelf()));
        currentNonce++;
    }
}
