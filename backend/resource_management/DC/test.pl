#!/usr/bin/perl


my $num = 40;
my $ingress = $num * 2;
for my $dc(1..$num){
    system("go run dc.go $dc $ingress > DC${dc}.json");
}
