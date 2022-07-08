package br.com.poc;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    public interface Command extends Serializable {
    }

    public static class InstructionCommand implements Command {
        private final String message;

        public InstructionCommand(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class ResultCommand implements Command {
        private final BigInteger bigInteger;

        public ResultCommand(BigInteger bigInteger) {
            this.bigInteger = bigInteger;
        }

        public BigInteger getBigInteger() {
            return bigInteger;
        }
    }

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    private final SortedSet<BigInteger> primes = new TreeSet<>();

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, command -> {
                    if (command.getMessage().equals("start")) {
                        IntStream.range(0, 20).forEach(i -> {
                            ActorRef<WorkerBehavior.Command> worker = getContext().spawn(WorkerBehavior.create(), "worker" + i);
                            worker.tell(new WorkerBehavior.Command("start", getContext().getSelf()));
                        });
                    }
                    return this;
                })
                .onMessage(ResultCommand.class, command -> {
                    primes.add(command.getBigInteger());
                    System.out.println("I have received: " + primes.size() + " prime numbers");
                    if (primes.size() == 20){
                        primes.forEach(System.out::println);
                    }
                    return this;
                })
                .build();
    }

}
