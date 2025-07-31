import { Search, Home, Heart, User } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { LoginButton } from '@/components/auth/login-button';

export default function HomePage() {
  return (
    <div className="min-h-screen bg-background">
      {/* Navigation */}
      <nav className="bg-white border-b border-gray-200 px-4 py-3">
        <div className="max-w-6xl mx-auto flex justify-between items-center">
          <div className="font-bold text-xl text-primary">HaniHome AU</div>
          <LoginButton />
        </div>
      </nav>

      {/* Hero Section */}
      <div className="relative bg-gradient-to-br from-primary/10 to-accent/10 py-20 px-4">
        <div className="max-w-6xl mx-auto text-center">
          <h1 className="text-5xl font-bold text-foreground mb-6">
            HaniHome AU
          </h1>
          <p className="text-xl text-muted-foreground mb-8 max-w-3xl mx-auto">
            한국인을 위한 호주 렌탈 플랫폼 - 믿을 수 있는 숙소를 쉽고 안전하게 찾아보세요
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button size="lg" className="bg-primary hover:bg-primary/90">
              <Search className="mr-2 h-5 w-5" />
              숙소 찾기
            </Button>
            <Button variant="outline" size="lg">
              <Home className="mr-2 h-5 w-5" />
              숙소 등록하기
            </Button>
          </div>
        </div>
      </div>

      {/* Search Section */}
      <div className="py-16 px-4 bg-white">
        <div className="max-w-4xl mx-auto">
          <div className="bg-white rounded-lg shadow-lg p-8 border border-border">
            <h2 className="text-2xl font-semibold mb-6 text-center">
              원하는 조건으로 숙소를 찾아보세요
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div>
                <label className="block text-sm font-medium mb-2">지역</label>
                <input
                  type="text"
                  placeholder="시드니, 멜버른..."
                  className="w-full p-3 border border-input rounded-md bg-input"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">가격대</label>
                <select className="w-full p-3 border border-input rounded-md bg-input">
                  <option>전체</option>
                  <option>$200-400/주</option>
                  <option>$400-600/주</option>
                  <option>$600+/주</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">침실</label>
                <select className="w-full p-3 border border-input rounded-md bg-input">
                  <option>전체</option>
                  <option>1개</option>
                  <option>2개</option>
                  <option>3개+</option>
                </select>
              </div>
              <div className="flex items-end">
                <Button size="lg" className="w-full">
                  <Search className="mr-2 h-5 w-5" />
                  검색
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div className="py-16 px-4 bg-muted/50">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl font-bold text-center mb-12">
            왜 HaniHome AU를 선택해야 할까요?
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center p-6">
              <div className="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-4">
                <Heart className="h-8 w-8 text-primary" />
              </div>
              <h3 className="text-xl font-semibold mb-3">한국인 맞춤 서비스</h3>
              <p className="text-muted-foreground">
                한국어 지원과 한국인 커뮤니티를 위한 특별한 서비스를 제공합니다.
              </p>
            </div>
            <div className="text-center p-6">
              <div className="w-16 h-16 bg-accent/10 rounded-full flex items-center justify-center mx-auto mb-4">
                <Home className="h-8 w-8 text-accent" />
              </div>
              <h3 className="text-xl font-semibold mb-3">검증된 숙소</h3>
              <p className="text-muted-foreground">
                모든 숙소는 철저한 검증을 거쳐 안전하고 신뢰할 수 있습니다.
              </p>
            </div>
            <div className="text-center p-6">
              <div className="w-16 h-16 bg-secondary/10 rounded-full flex items-center justify-center mx-auto mb-4">
                <User className="h-8 w-8 text-secondary-foreground" />
              </div>
              <h3 className="text-xl font-semibold mb-3">24/7 고객지원</h3>
              <p className="text-muted-foreground">
                언제든지 도움이 필요하면 한국어로 지원받을 수 있습니다.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="py-16 px-4 bg-primary text-primary-foreground">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-3xl font-bold mb-4">
            지금 바로 시작해보세요
          </h2>
          <p className="text-xl mb-8 opacity-90">
            무료 계정을 만들고 완벽한 숙소를 찾아보세요
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <LoginButton variant="secondary" size="lg" />
          </div>
        </div>
      </div>

      {/* Footer */}
      <footer className="bg-muted py-12 px-4">
        <div className="max-w-6xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div>
              <h3 className="font-bold text-lg mb-4">HaniHome AU</h3>
              <p className="text-sm text-muted-foreground">
                한국인을 위한 호주 렌탈 플랫폼
              </p>
            </div>
            <div>
              <h4 className="font-semibold mb-3">서비스</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li>숙소 검색</li>
                <li>숙소 등록</li>
                <li>커뮤니티</li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-3">고객지원</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li>FAQ</li>
                <li>연락처</li>
                <li>도움말</li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-3">회사정보</h4>
              <ul className="space-y-2 text-sm text-muted-foreground">
                <li>회사소개</li>
                <li>이용약관</li>
                <li>개인정보처리방침</li>
              </ul>
            </div>
          </div>
          <div className="border-t border-border mt-8 pt-8 text-center text-sm text-muted-foreground">
            © 2024 HaniHome AU. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  );
}
