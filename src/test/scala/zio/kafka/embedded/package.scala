package zio.kafka

import net.manub.embeddedkafka.{ EmbeddedKafka, EmbeddedKafkaConfig }
import zio._

package object embedded {
  type Kafka = Has[Kafka.Service]

  object Kafka {
    trait Service {
      def bootstrapServers: List[String]
      def stop(): UIO[Unit]
    }

    case class EmbeddedKafkaService(config: EmbeddedKafkaConfig) extends Service {
      override def bootstrapServers: List[String] = List(s"localhost:${config.kafkaPort}")
      override def stop(): UIO[Unit]              = ZIO.effectTotal(EmbeddedKafka.stop())
    }

    case object DefaultLocal extends Service {
      override def bootstrapServers: List[String] = List(s"localhost:9092")
      override def stop(): UIO[Unit]              = UIO.unit
    }

    val embedded: ZLayer[Any, Throwable, Kafka] = ZLayer.fromManaged {
      implicit val embeddedKafkaConfig = EmbeddedKafkaConfig(
        customBrokerProperties = Map("group.min.session.timeout.ms" -> "500", "group.initial.rebalance.delay.ms" -> "0")
      )
      ZManaged.make(ZIO.effect {
        EmbeddedKafka.start()
        EmbeddedKafkaService(embeddedKafkaConfig)
      })(_.stop())
    }

    val local: ZLayer[Any, Nothing, Kafka] = ZLayer.succeed(DefaultLocal)
  }
}
