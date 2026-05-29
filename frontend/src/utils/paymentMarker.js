const PAYMENT_MARKER_KEY = 'fitness.pendingPayment'

export function markPaymentStarted(order) {
  if (!order?.orderNo) return
  sessionStorage.setItem(PAYMENT_MARKER_KEY, JSON.stringify({
    orderNo: order.orderNo,
    orderType: order.orderType || '',
    startedAt: Date.now()
  }))
}

export function readPaymentMarker() {
  const raw = sessionStorage.getItem(PAYMENT_MARKER_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw)
  } catch (error) {
    sessionStorage.removeItem(PAYMENT_MARKER_KEY)
    return null
  }
}

export function clearPaymentMarker() {
  sessionStorage.removeItem(PAYMENT_MARKER_KEY)
}

export function isPaymentFinished(order) {
  return Boolean(order?.status && order.status !== 'PENDING')
}
