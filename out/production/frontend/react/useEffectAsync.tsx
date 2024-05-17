import { DependencyList, MutableRefObject, useEffect, useRef } from "react"


export function useEffectAsync(cb: (ref: MutableRefObject<boolean>) => Promise<void>, deps: DependencyList) {

  const isMounted = useRef(false)

  useEffect(() => {
    isMounted.current = true

    cb(isMounted).then()

    return () => {
      isMounted.current = false

    }
  }, deps)
}
