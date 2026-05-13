import test from 'node:test'
import assert from 'node:assert/strict'

import {
  buildWeekSessionCards,
  getBookingStats,
  filterBookingsByStatus,
  getTodayKey,
  getWeekEndKey
} from './bookings.utils.js'

test('getTodayKey returns current date in YYYY-MM-DD', () => {
  const date = new Date('2026-05-09T12:00:00')
  assert.equal(getTodayKey(date), '2026-05-09')
})

test('getWeekEndKey returns 6 days after today', () => {
  const date = new Date('2026-05-09T12:00:00')
  assert.equal(getWeekEndKey(date), '2026-05-15')
})

test('buildWeekSessionCards filters current week sessions and merges booking state', () => {
  const sessions = [
    {
      id: 101,
      courseId: 11,
      courseName: '晨间燃脂',
      coachName: '张教练',
      imageUrl: 'cover-a.jpg',
      sessionDate: '2026-05-10',
      startTime: '09:00:00',
      endTime: '10:00:00',
      bookedCount: 6
    },
    {
      id: 102,
      courseId: 12,
      courseName: '力量循环',
      coachName: '李教练',
      imageUrl: 'cover-b.jpg',
      sessionDate: '2026-05-10',
      startTime: '19:00:00',
      endTime: '20:00:00',
      bookedCount: 10
    },
    {
      id: 103,
      courseId: 13,
      courseName: '普拉提',
      coachName: '王教练',
      imageUrl: 'cover-c.jpg',
      sessionDate: '2026-05-11',
      startTime: '10:00:00',
      endTime: '11:00:00',
      bookedCount: 8
    },
    {
      id: 104,
      courseId: 14,
      courseName: '瑜伽',
      coachName: '赵教练',
      imageUrl: 'cover-d.jpg',
      sessionDate: '2026-05-16',
      startTime: '08:00:00',
      endTime: '09:00:00',
      bookedCount: 5
    }
  ]

  const bookings = [
    {
      id: 501,
      sessionId: 102,
      status: 1,
      sessionDate: '2026-05-10'
    },
    {
      id: 502,
      sessionId: 103,
      status: 2,
      sessionDate: '2026-05-11'
    }
  ]

  const cards = buildWeekSessionCards(sessions, bookings, '2026-05-09')

  assert.equal(cards.length, 3)
  assert.deepEqual(
    cards.map((card) => ({
      id: card.id,
      isBooked: card.isBooked,
      bookingId: card.bookingId ?? null
    })),
    [
      { id: 101, isBooked: false, bookingId: null },
      { id: 102, isBooked: true, bookingId: 501 },
      { id: 103, isBooked: false, bookingId: null }
    ]
  )
})

test('getBookingStats counts future active bookings as upcoming', () => {
  const bookings = [
    { id: 1, status: 0, sessionDate: '2026-05-10' },
    { id: 2, status: 1, sessionDate: '2026-05-11' },
    { id: 3, status: 1, sessionDate: '2026-05-08' },
    { id: 4, status: 2, sessionDate: '2026-05-07' },
    { id: 5, status: 3, sessionDate: '2026-05-06' }
  ]

  const stats = getBookingStats(bookings, '2026-05-09')

  assert.deepEqual(stats, {
    total: 5,
    upcoming: 2,
    completed: 1,
    cancelled: 1
  })
})

test('filterBookingsByStatus matches page filters', () => {
  const bookings = [
    { id: 1, status: 0 },
    { id: 2, status: 1 },
    { id: 3, status: 2 },
    { id: 4, status: 3 }
  ]

  assert.deepEqual(
    filterBookingsByStatus(bookings, 'booked').map((item) => item.id),
    [1, 2]
  )
  assert.deepEqual(
    filterBookingsByStatus(bookings, 'completed').map((item) => item.id),
    [4]
  )
  assert.deepEqual(
    filterBookingsByStatus(bookings, 'cancelled').map((item) => item.id),
    [3]
  )
  assert.deepEqual(
    filterBookingsByStatus(bookings, 'all').map((item) => item.id),
    [1, 2, 3, 4]
  )
})