function pad(value) {
  return String(value).padStart(2, '0')
}

export function toDateKey(value) {
  if (!value) {
    return ''
  }

  if (value instanceof Date) {
    return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}`
  }

  if (typeof value === 'string') {
    return value.includes('T') ? value.slice(0, 10) : value.slice(0, 10)
  }

  return ''
}

export function getTodayKey(today = new Date()) {
  const baseDate = today instanceof Date ? new Date(today) : new Date(today)
  return toDateKey(baseDate)
}

export function getWeekEndKey(today = new Date()) {
  const baseDate = today instanceof Date ? new Date(today) : new Date(today)
  baseDate.setDate(baseDate.getDate() + 6)
  return toDateKey(baseDate)
}

export function buildWeekSessionCards(sessions = [], bookings = [], today = new Date()) {
  const todayKey = getTodayKey(today)
  const weekEndKey = getWeekEndKey(today)
  const bookingMap = new Map()

  bookings
    .filter((booking) => booking.sessionId && [0, 1].includes(booking.status))
    .forEach((booking) => {
      bookingMap.set(booking.sessionId, booking)
    })

  return sessions
    .filter((session) => {
      const dateKey = toDateKey(session.sessionDate)
      return dateKey >= todayKey && dateKey <= weekEndKey
    })
    .map((session) => {
      const booking = bookingMap.get(session.id)
      return {
        ...session,
        isBooked: Boolean(booking),
        bookingId: booking?.id ?? null
      }
    })
}

export function getBookingStats(bookings = [], today = new Date()) {
  const todayKey = toDateKey(today)

  return {
    total: bookings.length,
    upcoming: bookings.filter(
      (booking) =>
        [0, 1].includes(booking.status) &&
        toDateKey(booking.sessionDate) >= todayKey
    ).length,
    completed: bookings.filter((booking) => booking.status === 3).length,
    cancelled: bookings.filter((booking) => booking.status === 2).length
  }
}

export function filterBookingsByStatus(bookings = [], filterStatus = 'all') {
  if (filterStatus === 'booked') {
    return bookings.filter((booking) => [0, 1].includes(booking.status))
  }
  if (filterStatus === 'completed') {
    return bookings.filter((booking) => booking.status === 3)
  }
  if (filterStatus === 'cancelled') {
    return bookings.filter((booking) => booking.status === 2)
  }
  return bookings
}