import React, { useState } from 'react'

export function Button() {
  const [count, setCount] = useState<number>(0)

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setCount(count + 1)
    console.log(`button clicked ${count} times`)
    console.log(event)
  }


  return (
    <button onClick={(event) => handleClick(event)}>
      Counter: {count}
    </button>
  )
}