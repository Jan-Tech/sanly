export function formatNin(nin: string): string {
  const digits = nin.replace(/\D/g, '')
  return digits.length === 11 ? digits : nin
}

export function validateNin(nin: string): boolean {
  return /^\d{11}$/.test(nin.trim())
}

export function displayNin(nin: string): string {
  const d = nin.replace(/\D/g, '')
  if (d.length !== 11) return nin
  return `${d.slice(0, 2)} ${d.slice(2, 7)} ${d.slice(7, 11)}`
}
