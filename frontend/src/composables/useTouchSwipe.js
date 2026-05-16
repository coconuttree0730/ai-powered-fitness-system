export function useTouchSwipe({ onSwipeLeft, onSwipeRight, threshold = 50 }) {
  let touchStartX = 0

  function handleTouchStart(e) {
    touchStartX = e.touches[0].clientX
  }

  function handleTouchEnd(e) {
    const touchEndX = e.changedTouches[0].clientX
    const diff = touchStartX - touchEndX

    if (Math.abs(diff) > threshold) {
      if (diff > 0 && onSwipeLeft) {
        onSwipeLeft()
      } else if (diff < 0 && onSwipeRight) {
        onSwipeRight()
      }
    }
  }

  return { handleTouchStart, handleTouchEnd }
}