import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { SessionProvider } from "../components/auth/session-provider";
import { SessionManager, SessionStatusIndicator } from "../components/auth/session-manager";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "HaniHome AU - Premium Property Rentals",
  description: "Find your perfect rental property in Australia with HaniHome AU. Trusted by tenants, landlords, and agents nationwide.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <SessionProvider>
          <SessionManager
            refreshThreshold={5}
            maxInactivityTime={30}
            warningDuration={5}
            enableActivityTracking={true}
          >
            {children}
            <SessionStatusIndicator />
          </SessionManager>
        </SessionProvider>
      </body>
    </html>
  );
}
