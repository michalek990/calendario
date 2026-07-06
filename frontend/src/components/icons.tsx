import type { SVGProps } from 'react'

function Icon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="20"
      height="20"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      {...props}
    />
  )
}

export function IconHome(props: SVGProps<SVGSVGElement>) {
  return (
    <Icon {...props}>
      <path d="M3 11.5 12 4l9 7.5" />
      <path d="M5.5 10v9.5h5V13h3v6.5h5V10" />
    </Icon>
  )
}

export function IconClock(props: SVGProps<SVGSVGElement>) {
  return (
    <Icon {...props}>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 2.5" />
    </Icon>
  )
}

export function IconCalendar(props: SVGProps<SVGSVGElement>) {
  return (
    <Icon {...props}>
      <rect x="3.5" y="5" width="17" height="15.5" rx="2" />
      <path d="M3.5 10h17" />
      <path d="M8 3v4M16 3v4" />
    </Icon>
  )
}

export function IconCheckCircle(props: SVGProps<SVGSVGElement>) {
  return (
    <Icon {...props}>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M8.2 12.2l2.6 2.6 5-5.4" />
    </Icon>
  )
}

export function IconFolder(props: SVGProps<SVGSVGElement>) {
  return (
    <Icon {...props}>
      <path d="M3.5 7a2 2 0 0 1 2-2h3.6l2 2H18.5a2 2 0 0 1 2 2v7.5a2 2 0 0 1-2 2h-13a2 2 0 0 1-2-2V7Z" />
    </Icon>
  )
}

export function IconBell(props: SVGProps<SVGSVGElement>) {
  return (
    <Icon {...props}>
      <path d="M6 9.5a6 6 0 0 1 12 0c0 4.5 1.8 5.5 1.8 5.5H4.2S6 14 6 9.5Z" />
      <path d="M9.8 18a2.2 2.2 0 0 0 4.4 0" />
    </Icon>
  )
}

export function IconUser(props: SVGProps<SVGSVGElement>) {
  return (
    <Icon {...props}>
      <circle cx="12" cy="8" r="3.6" />
      <path d="M4.5 19.5c0-4 3.4-6 7.5-6s7.5 2 7.5 6" />
    </Icon>
  )
}

export function IconSettings(props: SVGProps<SVGSVGElement>) {
  return (
    <Icon {...props}>
      <circle cx="12" cy="12" r="2.8" />
      <path d="M19.4 12.9a7.4 7.4 0 0 0 0-1.8l1.9-1.5-1.9-3.3-2.3.9a7.3 7.3 0 0 0-1.6-.9L15 3.5H9l-.5 2.8a7.3 7.3 0 0 0-1.6.9l-2.3-.9-1.9 3.3 1.9 1.5a7.4 7.4 0 0 0 0 1.8L2.7 14.4l1.9 3.3 2.3-.9c.5.4 1 .7 1.6.9L9 20.5h6l.5-2.8c.6-.2 1.1-.5 1.6-.9l2.3.9 1.9-3.3-1.9-1.5Z" />
    </Icon>
  )
}

export function IconLogOut(props: SVGProps<SVGSVGElement>) {
  return (
    <Icon {...props}>
      <path d="M9.5 20.5h-4a2 2 0 0 1-2-2v-13a2 2 0 0 1 2-2h4" />
      <path d="M16 16.5l4.5-4.5-4.5-4.5" />
      <path d="M20.2 12h-11" />
    </Icon>
  )
}

export function IconChevronDown(props: SVGProps<SVGSVGElement>) {
  return (
    <Icon {...props} width="14" height="14">
      <path d="M5 8.5l5 5 5-5" />
    </Icon>
  )
}
