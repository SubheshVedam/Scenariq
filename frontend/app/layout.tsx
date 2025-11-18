import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'Scenariq Routes',
  description: 'Discover scenic detours before jumping into Google Maps'
};

export default function RootLayout({
  children
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
