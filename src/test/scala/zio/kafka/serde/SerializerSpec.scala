package zio.kafka.serde

import zio.test.Assertion._
import zio.test._

object SerializerSpec extends DefaultRunnableSpec {
  override def spec = suite("Serializer")(
    suite("asOption")(
      testM("serialize None values to null") {
        assertM(stringSerializer.asOption.serialize("topic1", None))(isNull)
      },
      testM("serialize Some values") {
        checkM(Gen.anyString) { string =>
          assertM(stringSerializer.asOption.serialize("topic1", Some(string)))(
            equalTo(string.getBytes)
          )
        }
      }
    )
  )
  private lazy val stringSerializer: Serializer[Any, String] = Serde.string
}
