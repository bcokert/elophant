package util

import org.mockito.Mockito._

trait TestUtils {
  def withMock[M, O](mock: M)(stub: M => O, result: O)(test: M => Unit) = {
    when(stub(mock)).thenReturn(result)
    test(mock)
    stub(verify(mock, org.mockito.Mockito.atLeast(1)))
  }
}
